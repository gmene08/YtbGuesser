package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.dto.room.JoinRoomRequestDTO;
import com.gabmene.videoguesser.dto.match.MatchConfigRequestDTO;
import com.gabmene.videoguesser.dto.room.RoomResponseDTO;
import com.gabmene.videoguesser.dto.room.RoomUpdateRequestDto;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.entity.UserMatch;
import com.gabmene.videoguesser.enums.MatchStatus;
import com.gabmene.videoguesser.enums.RoomStatus;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ConflictException;
import com.gabmene.videoguesser.exception.ForbiddenException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.RoomRepository;
import com.gabmene.videoguesser.repository.UserMatchRepository;
import com.gabmene.videoguesser.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;
    private final UserMatchRepository userMatchRepository;

    private final GameNotificationService gameNotificationService;

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
        room.setMaxPlayers(AppConstants.ROOM_MAX_PLAYERS_DEFAULT);

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
            StringBuilder sb = new StringBuilder(AppConstants.ROOM_CODE_LENGTH);
            for (int i = 0; i < AppConstants.ROOM_CODE_LENGTH; i++) {
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
        Room roomSaved = roomRepository.save(roomJoined);

        gameNotificationService.sendRoomUpdate(buildRoomResponseDTO(roomSaved));

        return roomSaved;
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
        if(roomStarting.getUsers().isEmpty()) {
            throw new BusinessException("Room needs at least 1 players to start");
        }

        if(roomStarting.getUsers().size() > roomStarting.getMaxPlayers()) {
            throw new BusinessException("Room cannot have more than " + roomStarting.getMaxPlayers() + " players");
        }

        // Match is created right when the room starts
        Match match = matchService.createMatch(roomStarting, request);

        roomStarting.setStatus(RoomStatus.PLAYING);
        Room roomSaved = roomRepository.save(roomStarting);
        gameNotificationService.sendRoomUpdate(buildRoomResponseDTO(roomSaved));
        return roomSaved;

    }

    @Transactional
    public Room leaveRoom(String roomCode, Integer userLeavingId) {

        User userLeaving = userRepository.findById(userLeavingId).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Optional<Room> roomOptional = roomRepository.findByCode(roomCode);

        // if the room does not exist, clear the user's room and return null
        if (roomOptional.isEmpty()) {
            userLeaving.setRoom(null);
            userRepository.save(userLeaving);
            return null;
        }

        Room roomLeaving = roomOptional.get();

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

        // if the room is in PLAYING status, remove the user from the match
        if (roomLeaving.getStatus() == RoomStatus.PLAYING) {
            UserMatch userMatchLeaving = userMatchRepository.findByUserIdAndRoomIdAndStatus(userLeaving.getId(), roomLeaving.getId(), MatchStatus.PLAYING)
                    .orElseThrow(()-> new ResourceNotFoundException("UserMatch not found"));
            userMatchRepository.delete(userMatchLeaving);
            userMatchRepository.flush();

            Match matchLeaving = userMatchLeaving.getMatch();
            gameNotificationService.sendMatchUpdate(matchService.buildMatchResponseDTO(matchLeaving));

        }

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

        Room roomSaved = roomRepository.save(roomLeaving);
        gameNotificationService.sendRoomUpdate(buildRoomResponseDTO(roomSaved));
        return roomSaved;
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

    @Transactional
    public Room updateRoom(String roomCode, RoomUpdateRequestDto request) {
        Room roomToBeUpdated = roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not found"));

        // validate if the updated maxPlayers is less than the current number of players in the room
        if(userRepository.findAllByRoom(roomToBeUpdated).size() > request.getMaxPlayers()) {
            throw new BusinessException("Room cannot have less players than the maximum");
        }

        roomToBeUpdated.setMaxPlayers(request.getMaxPlayers());

        Room roomSaved = roomRepository.save(roomToBeUpdated);
        gameNotificationService.sendRoomUpdate(buildRoomResponseDTO(roomSaved));

        return roomSaved;
    }

    public RoomResponseDTO buildRoomResponseDTO(Room room) {
        return RoomResponseDTO.from(room);
    }

    @Transactional
    public void handleRoomDisconnect(Integer userId) {
        userRepository.findById(userId).ifPresent(user ->{
            if(user.getRoom() != null) {
                this.leaveRoom(user.getRoom().getCode(), userId);
            }

        });
    }
}
