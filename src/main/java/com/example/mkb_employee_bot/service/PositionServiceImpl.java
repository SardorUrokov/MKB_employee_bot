package com.example.mkb_employee_bot.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.entity.Position;
import com.example.mkb_employee_bot.entity.dto.PositionDTO;
import com.example.mkb_employee_bot.repository.PositionRepository;
import com.example.mkb_employee_bot.repository.ManagementRepository;
import com.example.mkb_employee_bot.service.interfaces.PositionService;

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
    public Position updatePosition(Long positionId, PositionDTO positionDTO) {

        final var position = positionRepository
                .findById(positionId)
                .orElseThrow();

        final var management = managementRepository
                .findById(positionDTO.getManagementId())
                .orElseThrow();

        position.setName(positionDTO.getName());
        position.setManagement(management);

        return positionRepository.save(position);
    }

    @Override
    public void deletePosition(String positionName) {
        positionRepository.updatePositionIsDeleted(positionName);
    }
}