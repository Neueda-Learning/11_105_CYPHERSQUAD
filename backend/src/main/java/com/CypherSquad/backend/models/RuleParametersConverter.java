package com.CypherSquad.backend.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Converter
public class RuleParametersConverter implements AttributeConverter<Map<String, Object>, String> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute) {
		Map<String, Object> safeAttribute = attribute == null ? Map.of() : attribute;
		try {
			return OBJECT_MAPPER.writeValueAsString(safeAttribute);
		} catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Unable to serialize rule parameters to JSON", ex);
		}
	}

	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return new LinkedHashMap<>();
		}
		try {
			return OBJECT_MAPPER.readValue(dbData, new TypeReference<LinkedHashMap<String, Object>>() {
			});
		} catch (IOException ex) {
			throw new IllegalArgumentException("Unable to parse rule parameters JSON", ex);
		}
	}
}