package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.JoinRoomRequestDTO;
import com.gabmene.videoguesser.dto.RoomResponseDTO;
import com.gabmene.videoguesser.dto.MatchConfigDTO;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<RoomResponseDTO> startRoom(@PathVariable String roomCode, @RequestBody MatchConfigDTO request) {
        Room roomStarted = roomService.startRoom(roomCode, request);
        return ResponseEntity.ok(new RoomResponseDTO(roomStarted));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable String roomCode) {
        Room room = roomService.findRoomByCode(roomCode);
        return ResponseEntity.ok(new RoomResponseDTO(room));
    }

    @DeleteMapping("/leave/{roomCode}")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomCode, @RequestParam Integer userId) {
        Room roomLeaving = roomService.leaveRoom(roomCode, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomCode}/kick/{targetUserId}")
    public ResponseEntity<RoomResponseDTO> kickPlayer(@PathVariable String roomCode, @PathVariable Integer targetUserId, @RequestParam Integer userId) {
        Room roomUpdated = roomService.kickPlayer(userId, targetUserId, roomCode);
        return ResponseEntity.ok(new RoomResponseDTO(roomUpdated));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAvailableCategories(){
        return ResponseEntity.ok(MatchCategory.valuesAsStrings());
    }
}
