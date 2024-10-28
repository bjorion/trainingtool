package org.jorion.trainingtool.dto.json;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class encapsulates information relative to the registrations.
 */
@Getter
public class StatDTO {

    public final String userName;
    public final List<StatRegDTO> registrations = new ArrayList<>();

    public StatDTO(String userName, Map<String, Integer> map) {

        this.userName = userName;
        for (Entry<String, Integer> entry : map.entrySet()) {
            StatRegDTO dto = new StatRegDTO(entry.getKey(), entry.getValue());
            registrations.add(dto);
        }
    }

}
