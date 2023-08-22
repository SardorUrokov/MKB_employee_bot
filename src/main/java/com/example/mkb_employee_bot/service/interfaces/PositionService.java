package com.example.mkb_employee_bot.service.interfaces;

import com.example.mkb_employee_bot.entity.Position;
import com.example.mkb_employee_bot.entity.dto.PositionDTO;

import java.util.List;

public interface PositionService {

    Position createPosition(PositionDTO positionDTO);

    List<Position> getPositionList();

    Position updatePosition(Long id, PositionDTO positionDTO);


    void deletePosition(String positionName);
}
