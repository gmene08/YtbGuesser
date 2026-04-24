package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.room.JoinRoomRequestDTO;
import com.gabmene.videoguesser.dto.match.MatchConfigRequestDTO;
import com.gabmene.videoguesser.dto.room.RoomUpdateRequestDto;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.enums.RoomStatus;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ConflictException;
import com.gabmene.videoguesser.exception.ForbiddenException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.RoomRepository;
import com.gabmene.videoguesser.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;

    @Transactional
    public Room createRoom(Integer ownerId) {
        // search owner
        User owner = userRepository.findById(ownerId).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        // if the owner already is the owner of another room, return the existing room
        Room existingRoom = roomRepository.findByOwner(owner).orElse(null);
        if(existingRoom != null) {
            return existingRoom;
        }

        Room room = new Room();

        // config room
        room.setCode(generateUniqueCode());
        room.setStatus(RoomStatus.WAITING);
        room.setMaxPlayers(5);

        room.setOwner(owner);
        room.setUsers(new java.util.ArrayList<>());
        room.getUsers().add(owner); // add the ownerId to the users list

        // synchronous save
        Room savedRoom = roomRepository.save(room);
        owner.setRoom(savedRoom);

        return savedRoom;
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        String code;

        do {
            StringBuilder sb = new StringBuilder(5);
            for (int i = 0; i < 5; i++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (roomRepository.existsByCode(code));

        return code;
    }

    public Room findRoomByCode(String roomCode) {
        return roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not found"));
    }

    @Transactional
    public Room joinRoom(String roomCode, JoinRoomRequestDTO user) {

        // find room by code
        Room roomJoined = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // find user by id
        User userJoining = userRepository.findById(user.getUserId()).orElseThrow(()-> new ResourceNotFoundException("User not found"));


        // validate if the user is already in the room
        if (roomJoined.getUsers().contains(userJoining)) {
            return roomJoined;
        }

        // validate if the user is already in a room
        if(userJoining.getRoom() != null) {
            throw new ConflictException("User is already in another room");
        }

        // validate room max player count
        if(roomJoined.getUsers().size() >= roomJoined.getMaxPlayers()) {
            throw new BusinessException("Room is full");
        }
        // validate room status
        if(roomJoined.getStatus() != RoomStatus.WAITING) {
            throw new ConflictException("Room has already started ");
        }

        // add user to the room
        roomJoined.addUser(userJoining);
        roomRepository.save(roomJoined);

    return roomJoined;
    }

    @Transactional
    public Room startRoom(String roomCode, MatchConfigRequestDTO request) {

        // find the room
        Room roomStarting = roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not found"));

        System.out.println("Room starting: " + roomStarting);
        System.out.println("Request Categories: " + request.getCategories());
        System.out.println("Request Number of Rounds: " + request.getNumberOfRounds());
        System.out.println("User trying to start: " + request.getUserId());

        // validations
        if (roomStarting.getOwner() == null) {
            throw new ResourceNotFoundException("Room owner not found");
        }
        if(!roomStarting.getOwner().getId().equals(request.getUserId())) {
            throw new ForbiddenException("Only the ownerId can start the room");
        }
        if(roomStarting.getStatus() != RoomStatus.WAITING) {
            throw new ConflictException("Room has already started");
        }
        if(roomStarting.getUsers().size() < 1) {
            throw new BusinessException("Room needs at least 1 players to start");
        }

        if(roomStarting.getUsers().size() > roomStarting.getMaxPlayers()) {
            throw new BusinessException("Room cannot have more than " + roomStarting.getMaxPlayers() + " players");
        }

        // Match is created right when the room starts
        Match match = matchService.createMatch(roomStarting, request);

        roomStarting.setStatus(RoomStatus.PLAYING);
        return roomRepository.save(roomStarting);

    }

    @Transactional
    public Room leaveRoom(String roomCode, Integer userLeavingId) {

        Room roomLeaving = roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not found"));

        User userLeaving = userRepository.findById(userLeavingId).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if(userLeaving.getRoom() == null) {
            throw new ConflictException("User is not in a room");
        }

        if(!roomLeaving.getUsers().contains(userLeaving)) {
            throw new ConflictException("User is not in the room");
        }

        List<User> playersInRoom = roomLeaving.getUsers();
        User roomOwner = roomLeaving.getOwner();

        // remove the user from the match
        playersInRoom.remove(userLeaving);
        userLeaving.setRoom(null);
        userRepository.save(userLeaving);

        // if the room is empty, delete it - if the owner leaves, assign a new owner
        if(playersInRoom.isEmpty()) {

            roomRepository.delete(roomLeaving);
            return null;

        }
        else if (roomOwner != null && roomOwner.equals(userLeaving)) {
            roomLeaving.setOwner(null);

            int randomPlayerIndex = (int) (Math.random() * playersInRoom.size());
            User newOwner = playersInRoom.get(randomPlayerIndex);

            roomLeaving.setOwner(newOwner);

        }

        roomRepository.save(roomLeaving);
        return roomLeaving;
    }

    @Transactional
    public Room kickPlayer(Integer userId, Integer targetUserId, String roomCode){
        Room room = roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not found"));

        if(userId == null || targetUserId == null) {
            throw new BusinessException("User ids cannot be null");
        }

        // validate if the user is the owner
        if(room.getOwner() == null || !room.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the owner can kick a player");
        }

        // validate if the user is trying to kick themselves
        if (userId.equals(targetUserId)) {
            throw new ForbiddenException("You cannot kick yourself");
        }

        return leaveRoom(roomCode, targetUserId);
    }

    public Room updateRoom(String roomCode, RoomUpdateRequestDto request) {
        Room roomUpdated = roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not found"));

        // validate if the updated maxPlayers is less than the current number of players in the room
        if(userRepository.findAllByRoom(roomUpdated).size() > request.getMaxPlayers()) {
            throw new BusinessException("Room cannot have less players than the maximum");
        }

        roomUpdated.setMaxPlayers(request.getMaxPlayers());

        return roomRepository.save(roomUpdated);
    }
}
