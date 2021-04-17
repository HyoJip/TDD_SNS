package com.hoaxify.hoxaxify.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hoaxify.hoxaxify.common.GeneralResponse;
import com.hoaxify.hoxaxify.model.Hoax;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.model.vm.HoaxVM;
import com.hoaxify.hoxaxify.service.HoaxService;
import com.hoaxify.hoxaxify.validation.CurrentUser;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

	@Autowired
	HoaxService hoaxService;
	
	@PostMapping("/hoaxes")
	public HoaxVM createHoax(@CurrentUser User user, @Valid @RequestBody Hoax hoax) {
		return new HoaxVM(hoaxService.postHoax(user, hoax));
	}
	
	@GetMapping({"/hoaxes", "/users/{username}/hoaxes"})
	public Page<HoaxVM> getAllHoaxes(@PathVariable(required = false) String username, Pageable pageable) {
		return hoaxService.getAllHoaxes(username, pageable).map(HoaxVM::new);
	}
	
	@GetMapping({"/hoaxes/{id:[\\d]+}", "/users/{username}/hoaxes/{id:[\\d]+}"})
	public ResponseEntity<?> getHoaxesRelative(
										  @PathVariable long id
										, @PathVariable(required = false) String username
										, @RequestParam(defaultValue = "after") String direction
										, @RequestParam(required = false, defaultValue = "false") boolean count
										, Pageable pageable) {
		if (direction.equalsIgnoreCase("before")) {
			return ResponseEntity.ok(hoaxService.getOldHoaxes(username, id, pageable).map(HoaxVM::new));
		} else {
			if (count) {
				return ResponseEntity.ok(hoaxService.getCountOfNewHoaxes(username, id));
			}
			return ResponseEntity.ok(hoaxService.getNewHoaxes(username, id, pageable).stream().map(HoaxVM::new));
		}
	}
	
	@DeleteMapping("/hoaxes/{id:[\\d]+}")
	@PreAuthorize("@hoaxSecurityService.isAllowedToDelete(#id, principal)")
	public GeneralResponse deleteHoax(@PathVariable long id) {
		hoaxService.removeHoax(id);
		return new GeneralResponse("피드 삭제 완료");
	}
}
