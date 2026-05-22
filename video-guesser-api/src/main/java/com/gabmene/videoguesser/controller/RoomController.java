package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.dto.room.JoinRoomRequestDTO;
import com.gabmene.videoguesser.dto.room.RoomResponseDTO;
import com.gabmene.videoguesser.dto.match.MatchConfigRequestDTO;
import com.gabmene.videoguesser.dto.room.RoomUpdateRequestDto;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RoomController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/{ownerId}")
    public ResponseEntity<RoomResponseDTO> createRoom(@PathVariable Integer ownerId) {
        Room room = roomService.createRoom(ownerId);
        return ResponseEntity.ok(roomService.buildRoomResponseDTO(room));
    }

    @PostMapping("/join/{roomCode}")
    public ResponseEntity<RoomResponseDTO> joinRoom(@PathVariable String roomCode,@Valid @RequestBody JoinRoomRequestDTO request) {
        Room room = roomService.joinRoom(roomCode, request);
        return ResponseEntity.ok(roomService.buildRoomResponseDTO(room));
    }

    @PatchMapping("/{roomCode}/start")
    public ResponseEntity<RoomResponseDTO> startRoom(@PathVariable String roomCode, @Valid @RequestBody MatchConfigRequestDTO request) {
        Room room = roomService.startRoom(roomCode, request);
        return ResponseEntity.ok(roomService.buildRoomResponseDTO(room));
    }

    @PatchMapping("/{roomCode}")
    public ResponseEntity<RoomResponseDTO> updateRoom(@PathVariable String roomCode,@Valid @RequestBody RoomUpdateRequestDto request) {
        Room room = roomService.updateRoom(roomCode, request);
        return ResponseEntity.ok(roomService.buildRoomResponseDTO(room));
    }

    //TODO: implement this
    @PatchMapping("/{roomCode}/end")
    public ResponseEntity<RoomResponseDTO> endRoom(@PathVariable String roomCode, @RequestParam Integer userId) {

        return null;
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable String roomCode) {
        Room room = roomService.findRoomByCode(roomCode);
        return ResponseEntity.ok(roomService.buildRoomResponseDTO(room));
    }

    @DeleteMapping("/leave/{roomCode}")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomCode, @RequestParam Integer userId) {
        roomService.leaveRoom(roomCode, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomCode}/kick/{targetUserId}")
    public ResponseEntity<RoomResponseDTO> kickPlayer(@PathVariable String roomCode, @PathVariable Integer targetUserId, @RequestParam Integer userId) {
        Room room = roomService.kickPlayer(userId, targetUserId, roomCode);
        return ResponseEntity.ok(roomService.buildRoomResponseDTO(room));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAvailableCategories(){
        return ResponseEntity.ok(MatchCategory.valuesAsStrings());
    }
}
