package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.entity.Management;
import com.example.mkb_employee_bot.entity.Position;
import com.example.mkb_employee_bot.entity.dto.PositionDTO;
import com.example.mkb_employee_bot.repository.ManagementRepository;
import com.example.mkb_employee_bot.repository.PositionRepository;
import com.example.mkb_employee_bot.service.interfaces.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final ManagementRepository managementRepository;

    @Override
    public Position createPosition(PositionDTO positionDTO) {

        final var management = managementRepository
                .findById(positionDTO.getManagementId())
                .orElseThrow();

        return positionRepository.save(
                new Position(
                        positionDTO.getName(),
                        management
                )
        );
    }

    @Override
    public List<Position> getPositionList() {
        return positionRepository.findAll();
    }

    @Override
    public Position updatePosition(Long id, PositionDTO positionDTO) {
        return null;
    }

    @Override
    public void deletePosition(String positionName) {
        positionRepository.updatePositionIsDeleted(positionName);
    }
}