package com.miage.pouleAPI.adapters;

import com.miage.pouleAPI.dtos.profile.UserProfileResponseDTO;
import com.miage.pouleAPI.entity.ApplicationUser;

import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserAdapter {

    // Conversion classique Entité -> DTO de réponse
    @Mapping(source = "country.code", target = "countryCode")
    UserProfileResponseDTO toResponseDTO(ApplicationUser user);

    // Mise à jour de l'entité EXISTANTE à partir du DTO
    //@MappingTarget pour que MapStruct modifie l'objet 'user' au lieu d'en créer un nouveau
    void updateEntityFromDto(UpdateProfileRequestDTO dto, @MappingTarget ApplicationUser user);
}