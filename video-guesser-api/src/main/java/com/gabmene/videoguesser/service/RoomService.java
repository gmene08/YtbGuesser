package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.JoinRoomRequestDTO;
import com.gabmene.videoguesser.dto.StartRoomRequestDTO;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.enums.RoomStatus;
import com.gabmene.videoguesser.repository.RoomRepository;
import com.gabmene.videoguesser.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public Room createRoom(Room room, Integer ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(()-> new RuntimeException("User not found"));

        room.setCode(generateUniqueCode());
        room.setOwner(owner);
        room.setStatus(RoomStatus.WAITING);

        Room savedRoom = roomRepository.save(room);
        owner.setRoom(savedRoom);
        userRepository.save(owner);

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

    @Transactional
    public Room joinRoom(String roomCode, JoinRoomRequestDTO user) {

        // find room by code
        Room roomJoined = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // find user by id
        User userJoining = userRepository.findById(user.getUserId()).orElseThrow(()-> new RuntimeException("User not found"));

        // validate room max player count
        if(roomJoined.getUsers().size() >= roomJoined.getMaxPlayers()) {
            throw new RuntimeException("Room is full");
        }
        // validate room status
        if(roomJoined.getStatus() != RoomStatus.WAITING) {
            throw new RuntimeException("Room has already started ");
        }
        // validate if the user is already in the room
        if (roomJoined.getUsers().contains(userJoining)) {
            throw new RuntimeException("User is already in the room");
        }

        // add user to the room
        roomJoined.addUser(userJoining);
        roomRepository.save(roomJoined);

    return roomJoined;
    }

    @Transactional
    public Room startRoom(String roomCode, StartRoomRequestDTO request) {

        // find the room
        Room roomStarting = roomRepository.findByCode(roomCode).orElseThrow(()-> new RuntimeException("Room not found"));

        // validations
        if(!roomStarting.getOwner().getId().equals(request.getUserId())) {
            throw new RuntimeException("Only the owner can start the room");
        }
        if(roomStarting.getStatus() != RoomStatus.WAITING) {
            throw new RuntimeException("Room has already started");
        }
        if(roomStarting.getUsers().size() < 2) {
            throw new RuntimeException("Room needs at least 2 players to start");
        }

        roomStarting.setStatus(RoomStatus.PLAYING);
        roomRepository.save(roomStarting);

        return roomStarting;
    }
}
