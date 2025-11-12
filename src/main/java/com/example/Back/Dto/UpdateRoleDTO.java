// Crie este novo arquivo: com.example.Back.Dto.UpdateRoleDTO.java
package com.example.Back.Dto;

import com.example.Back.Entity.UserRole;

// Este DTO simples carrega apenas a nova função (role)
public record UpdateRoleDTO(UserRole role) {}