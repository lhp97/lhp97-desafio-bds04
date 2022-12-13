package com.devsuperior.bds04.services;

import com.devsuperior.bds04.dto.EventDTO;
import com.devsuperior.bds04.entities.City;
import com.devsuperior.bds04.entities.Event;
import com.devsuperior.bds04.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        Event event = new Event();
        convertEntityDTO(eventDTO, event);
        event = eventRepository.save(event);
        return new EventDTO(event);
    }

    @Transactional
    public Page<EventDTO> findAllEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);
        return events.map(x -> new EventDTO(x));
    }

    private void convertEntityDTO(EventDTO eventDTO, Event event) {
        event.setName(eventDTO.getName());
        event.setDate(eventDTO.getDate());
        event.setUrl(eventDTO.getUrl());
        event.setCity(new City(eventDTO.getCityId(), ""));
    }
}
