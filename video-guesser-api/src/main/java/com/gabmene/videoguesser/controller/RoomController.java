package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.JoinRoomRequestDTO;
import com.gabmene.videoguesser.dto.RoomResponseDTO;
import com.gabmene.videoguesser.dto.StartRoomRequestDTO;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/{ownerId}")
    public ResponseEntity<RoomResponseDTO> createRoom(@PathVariable Integer ownerId) {
        Room newRoom = roomService.createRoom(ownerId);
        return ResponseEntity.ok(new RoomResponseDTO(newRoom));
    }

    @PostMapping("/join/{roomCode}")
    public ResponseEntity<RoomResponseDTO> joinRoom(@PathVariable String roomCode, @RequestBody JoinRoomRequestDTO request) {
        Room roomJoined = roomService.joinRoom(roomCode, request);
        return ResponseEntity.ok(new RoomResponseDTO(roomJoined));
    }

    @PatchMapping("/{roomCode}/start")
    public ResponseEntity<RoomResponseDTO> startRoom(@PathVariable String roomCode, @RequestBody StartRoomRequestDTO request) {
        Room roomStarted = roomService.startRoom(roomCode, request);
        return ResponseEntity.ok(new RoomResponseDTO(roomStarted));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable String roomCode) {
        Room room = roomService.findRoomByCode(roomCode);
        return ResponseEntity.ok(new RoomResponseDTO(room));
    }

    @PostMapping("/leave/{roomCode}")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomCode, @RequestBody JoinRoomRequestDTO request) {
        Room roomLeaving = roomService.leaveRoom(roomCode, request.getUserId());
        return ResponseEntity.ok().build();
    }
}
