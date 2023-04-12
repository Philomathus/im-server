package com.feiwin.imserver.controller;

import com.feiwin.imserver.annotation.FieldName;

import com.feiwin.imserver.dto.RoomMessageDto;
import com.feiwin.imserver.dto.RoomDto;
import com.feiwin.imserver.service.ImService;
import com.feiwin.imserver.service.TokenService;
import com.feiwin.imserver.vo.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.feiwin.imserver.constant.Constants.USERNAME;
import static com.feiwin.imserver.constant.Constants.TOKEN;

@RestController
@RequestMapping("/room")
public class RoomController {

    @Resource
    private ImService imService;

    @Resource
    private TokenService tokenService;

    @PostMapping
    public Response<?> create(@RequestBody RoomDto roomDto, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.createRoom(username, roomDto.getRoomId(), getSettings(roomDto));
        return Response.ok();
    }

    @PatchMapping("/join")
    public Response<?> join(@RequestBody RoomDto roomDto, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.joinRoom(username, roomDto.getRoomId());
        return Response.ok();
    }

    @PostMapping("/message")
    public Response<?> sendMessage(@RequestBody RoomMessageDto roomMessageDto, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.sendRoomMessage(username, roomMessageDto.getRoomId(), roomMessageDto.getContent());
        return Response.ok();
    }

    @PatchMapping("/leave")
    public Response<?> leave(@RequestBody RoomDto roomDto, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.leaveRoom(username, roomDto.getRoomId());
        return Response.ok();
    }

    @DeleteMapping("/{roomId}")
    public Response<?> destroy(@PathVariable String roomId, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        imService.destroyRoom(username, roomId);
        return Response.ok();
    }

    @GetMapping
    public Response<?> getAll() {
        return Response.ok(imService.getAllRooms());
    }

    @GetMapping("/joined")
    public Response<?> getJoined(@RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        return Response.ok(imService.getJoinedRooms(username));
    }

    @GetMapping("/{roomId}/occupant")
    public Response<?> getOccupants(@PathVariable String roomId, @RequestHeader(TOKEN) String token) {
        String username = tokenService.getClaimsValueFromToken(USERNAME, token);
        return Response.ok(imService.getRoomOccupants(username, roomId));
    }

    private static Map<String, String> getSettings(RoomDto roomDto) {
        Map<String, String> settings = new HashMap<>();

        for(Field field : RoomDto.class.getDeclaredFields()) {
            FieldName fieldNameAnnotation = field.getAnnotation(FieldName.class);

            if(fieldNameAnnotation != null) {
                field.setAccessible(true);

                try {
                    settings.put(fieldNameAnnotation.value(), Objects.toString(field.get(roomDto), null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return settings;
    }
}
