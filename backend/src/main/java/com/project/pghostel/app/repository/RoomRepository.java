package com.project.pghostel.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import com.project.pghostel.app.entity.Room;


public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomNo(String roomNo);

    List<Room> findAllByOrderByIdAsc();

}
