package com.devsuperior.bds04.services;

import com.devsuperior.bds04.dto.RoleDTO;
import com.devsuperior.bds04.dto.UserDTO;
import com.devsuperior.bds04.dto.UserInsertDTO;
import com.devsuperior.bds04.dto.UserUpdateDTO;
import com.devsuperior.bds04.entities.Role;
import com.devsuperior.bds04.entities.User;
import com.devsuperior.bds04.repositories.RoleRepository;
import com.devsuperior.bds04.repositories.UserRepository;
import com.devsuperior.bds04.services.exceptions.DataBaseException;
import com.devsuperior.bds04.services.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Transactional
	public List<UserDTO> findAll() {
		List<User> users = userRepository.findAll();
		return convertListToDTO(users);
	}

	@Transactional
	public Page<UserDTO> findAllPaged(Pageable pageable) {
		Page<User> usersPaged = userRepository.findAll(pageable);
		return usersPaged.map(user -> new UserDTO(user));
	}

	@Transactional
	public UserDTO findById(Long id) {
		Optional<User> optionalUser = userRepository.findById(id);
		User user = optionalUser.orElseThrow(() -> new ResourceNotFoundException("User não encontrado."));
		return new UserDTO(user);
	}

	@Transactional
	public UserDTO createUser(UserInsertDTO userInsertDTO) {
		User user = new User();
		convertDtoToEntity(userInsertDTO, user);
		user.setPassword(passwordEncoder.encode(userInsertDTO.getPassword()));
		user = userRepository.save(user);
		return new UserDTO(user);
	}

	@Transactional
	public UserDTO updateUser(Long id, UserUpdateDTO userDTO) {
		try {
			User user = userRepository.getOne(id);
			convertDtoToEntity(userDTO, user);
			user = userRepository.save(user);
			return new UserDTO(user);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found: " + id);
		}
	}

	public void deleteUser(Long id) {
		try {
			userRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found: " + id);
		} catch (DataIntegrityViolationException ex) {
			throw new DataBaseException("Integrity violation");
		}
	}

	private void convertDtoToEntity(UserDTO userDTO, User user) {
		user.setEmail(userDTO.getEmail());
		user.getRoles().clear();

		for (RoleDTO roleDTO : userDTO.getRoles()) {
			Role role = roleRepository.getOne(roleDTO.getId());
			user.getRoles().add(role);
		}
	}

	private List<UserDTO> convertListToDTO(List<User> users) {
		return users.stream().map(x -> new UserDTO(x)).collect(Collectors.toList());
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username);

		if (user == null) {
			log.error("User not found: " + username);
			throw new UsernameNotFoundException("Email not found");
		}

		log.info("User found: " + username);
		return user;
	}
}
