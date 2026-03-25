package com.miage.pouleAPI.adapters;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.ChampionshipRepository;
import com.miage.pouleAPI.repositories.CompetitionRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitionJpaAdapter {

    private final CompetitionRepository repository;
    private final ChampionshipRepository championshipRepository;
    private final ApplicationUserRepository userRepository;

    public CompetitionJpaAdapter(CompetitionRepository repository, ChampionshipRepository championshipRepository, ApplicationUserRepository userRepository){
        this.repository=repository;
        this.championshipRepository = championshipRepository;
        this.userRepository = userRepository;
    }


    public List<CompetitionDTO> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    public Optional<CompetitionDTO> findById(Integer id) {
        return repository.findById(id).map(this::toDomain);
    }

    public CompetitionDTO save(CreateCompetitionRequestDTO model) {
        Competition entity = toEntityRequest(model);
        Competition saved = repository.save(entity);
        return toDomain(saved);
    }

    public CompetitionDTO update (CompetitionDTO competitionDTO) {
        Competition entity = toEntity(competitionDTO);
        return toDomain(repository.save(entity));
    }

    public List<CompetitionDTO> findByChampionshipId(Integer championshipId) {
        return repository.findByChampionship_Id(championshipId)
                .stream()
                .map(this::toDomain)
                .toList();
    }
    
    public Optional<CompetitionDTO> findByName(String name) {
        return repository.findByName(name).map(this::toDomain);
    }

    // Méthodes pour gérer les observateurs
    public void addObserver(Integer competitionId, Integer userId) {
        Competition competition = repository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        competition.attach(user);
        repository.save(competition);
    }

    public void removeObserver(Integer competitionId, Integer userId) {
        Competition competition = repository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        competition.detach(user);
        repository.save(competition);
    }

    public List<Integer> getObserverIds(Integer competitionId) {
        return repository.findById(competitionId)
                .map(Competition::getObservers)
                .map(observers -> observers.stream().map(ApplicationUser::getId).toList())
                .orElse(List.of());
    }

    private CompetitionDTO toDomain(Competition competition){
        if (competition == null){
            return  null;
        }
        return new CompetitionDTO(
                competition.getChampionship().getId(),
                competition.getDescription(),
                competition.getEnd(),
                competition.getId(),
                competition.getName(),
                competition.getStart());
    }

    private Competition toEntity(CompetitionDTO competition) {
        if (competition == null) {
            return null;
        }
        Championship championship = championshipRepository.findById(competition.getChampionshipId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Championship not found with id " + competition.getChampionshipId()
                ));

        return new Competition(
                competition.getId(),
                competition.getName(),
                competition.getDescription(),
                championship,
                competition.getStart(),
                competition.getEnd()
        );
    }

    private Competition toEntityRequest(CreateCompetitionRequestDTO dto){
        if (dto == null){
            return null;
        }
        Competition entity = new Competition();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStart(dto.getStart());
        entity.setEnd(dto.getEnd());
        entity.setChampionship(championshipRepository.findById(dto.getChampionshipId()).get());
        return entity;
    }
}
