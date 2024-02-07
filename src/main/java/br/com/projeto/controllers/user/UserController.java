package br.com.projeto.controllers.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projeto.services.auth.AuthServices;
import br.com.projeto.services.user.UsersVOServices;
import br.com.projeto.util.UserAccessValid;
import br.com.projeto.vo.security.AccountCredentialsVO;
import br.com.projeto.vo.user.UserEnabledVO;
import br.com.projeto.vo.user.UserPasswordVO;
import br.com.projeto.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Users administration")
@RestController
@RequestMapping("/api/user")
public class UserController {
	
	@Autowired
	AuthServices authServices;
	
	@Autowired
	private UsersVOServices service;

	@Autowired
	UserAccessValid userAccessValid;
	
	@Operation(summary = "List all users, note : password is null in this case", 
			   description="List all users, note : password is null in this case",
			   responses = {
							@ApiResponse(description = "Success", responseCode = "200",
								content = {
									@Content(
										mediaType = "application/json",
										array = @ArraySchema(schema = @Schema(implementation = UserVO.class))
									)
								}),
							@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
							@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
							@ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
							@ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
						}
			   )
	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	public List<UserVO> findAllUserse(@RequestHeader("Authorization") String tokenJWT) {
		// return all users
		if (!userAccessValid.userAccess(tokenJWT, "user", "GET")) {
			throw new BadCredentialsException("User not authorized!");
		}
		List<UserVO> usersVO = service.loadUsersVO();
		return usersVO;
	}

	@Operation(summary = "Return a existing user, note : password is null in this case",
			responses = {
				@ApiResponse(description = "Success", responseCode = "200",
					content = @Content(schema = @Schema(implementation = UserVO.class))
				),
				@ApiResponse(description = "No Content", responseCode = "204", content = @Content),
				@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
				@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
				@ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
				@ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
			}
			)
	@GetMapping(value = "/{userName}", 
					produces=MediaType.APPLICATION_JSON_VALUE)
	public UserVO findByUserName(@PathVariable(value="userName") String userName, @RequestHeader("Authorization") String tokenJWT) {
		// return a especific user
		if (!userAccessValid.userAccess(tokenJWT, "username", "GET", userName)) {
			throw new BadCredentialsException("User not authorized!");
		}
		UserVO userVO = service.loadUserVOByUsername(userName);
		return userVO;
	}
	
	@Operation(summary = "Insert a new user",
			responses = {
				@ApiResponse(description = "Success", responseCode = "200",
					content = @Content(schema = @Schema(implementation = UserVO.class))
				),
				@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
				@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
				@ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
			}
			)
	@PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE,
				 produces=MediaType.APPLICATION_JSON_VALUE)
	public UserVO create(@RequestBody UserVO userVO, @RequestHeader("Authorization") String tokenJWT) {
		// add a new user
		if (checkIfParamsIsNotNull(userVO.getUserName(),userVO.getFullName(),userVO.getPassword()))
			throw new UsernameNotFoundException("Invalid client request!");
		if (!userAccessValid.userAccess(tokenJWT, "user", "POST")) {
			throw new BadCredentialsException("User not authorized!");
		}
		return service.createUser(userVO);
	}

	@Operation(summary = "Update a existing user, note : password is null in this case",
				responses = {
					@ApiResponse(description = "Updated", responseCode = "200",
						content = @Content(schema = @Schema(implementation = UserVO.class))
					),
					@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
					@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					@ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
					@ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
				}
			)
	@PutMapping(consumes=MediaType.APPLICATION_JSON_VALUE,
				 produces=MediaType.APPLICATION_JSON_VALUE)
	public UserVO Update(@RequestBody UserVO userVO, @RequestHeader("Authorization") String tokenJWT) {
		// update a user
		if (checkIfParamsIsNotNull(userVO.getUserName(),userVO.getFullName()))
			throw new UsernameNotFoundException("Invalid client request!");
		if (!userAccessValid.userAccess(tokenJWT, "user", "PUT")) {
			throw new BadCredentialsException("User not authorized!");
		}
		return service.updateUser(userVO);
	}

	@Operation(summary = "Update the password user",
			responses = {
				@ApiResponse(description = "No Content", responseCode = "204", content = @Content),
				@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
				@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
				@ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
				@ApiResponse(description = "Internal Error", responseCode = "500", content = @Content),
			})
	@PutMapping(value="/updatepassword",
				consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> UpdatePassword(@RequestBody UserPasswordVO userPasswordVO, @RequestHeader("Authorization") String tokenJWT) {
		// update password user
		if (checkIfParamsIsNotNull(userPasswordVO.getUserName(),userPasswordVO.getOldPassword(),userPasswordVO.getNewPassword()))
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
		if (!userAccessValid.userAccess(tokenJWT, "userpassword", "PUT", userPasswordVO.getUserName())) {
			throw new BadCredentialsException("User not authorized!");
		}
		
		// primeiro - Utilizar o signin para validar o usuario e senha anterior, isso é feito através do authenticationmaneger do spring security
		// deve ser chamado no controller!
		AccountCredentialsVO data = new AccountCredentialsVO(userPasswordVO.getUserName(),userPasswordVO.getOldPassword());
		var token = authServices.signin(data);
		if (token == null) return ResponseEntity.badRequest().build();
		// Pronto, se passou no signin, então existe esse usuario e senha
		// agora basta alterar a senha!
		service.updatePassword(userPasswordVO);
		return ResponseEntity.noContent().build();
	}
	
	@Operation(summary = "Update the password from a existing user, note : used only for ADMIN´s, the oldPassword is null in this case",
			responses = {
				@ApiResponse(description = "No Content", responseCode = "204", content = @Content),
				@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
				@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
				@ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
				@ApiResponse(description = "Internal Error", responseCode = "500", content = @Content),
			})
	@PutMapping(value="/admupdatepassword",
				consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> AdmUpdatePassword(@RequestBody UserPasswordVO userPasswordVO, @RequestHeader("Authorization") String tokenJWT) {
		// update password user from admin
		if (checkIfParamsIsNotNull(userPasswordVO.getUserName(),userPasswordVO.getNewPassword()))
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
		if (!userAccessValid.userAccess(tokenJWT, "admupdatepassword", "PUT")) {
			throw new BadCredentialsException("User not authorized!");
		}		
		service.updatePassword(userPasswordVO);
		return ResponseEntity.noContent().build();
	}	
	
	@Operation(summary = "Enabled or Disable a existing user",
			responses = {
				@ApiResponse(description = "No Content", responseCode = "204", content = @Content),
				@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
				@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
				@ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
				@ApiResponse(description = "Internal Error", responseCode = "500", content = @Content),
			})
	@PutMapping(value="/enabledordisable",
				consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> EnabladedOrDisableUser(@RequestBody UserEnabledVO userEnabledVO, @RequestHeader("Authorization") String tokenJWT) {
		// update password user
		if (checkIfParamsIsNotNull(userEnabledVO.getUserName()))
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
		if (!userAccessValid.userAccess(tokenJWT, "userenabled", "PUT")) {
			throw new BadCredentialsException("User not authorized!");
		}

		service.disableOrEnableUser(userEnabledVO);
		return ResponseEntity.noContent().build();
	}

	
	private boolean checkIfParamsIsNotNull(String param1) {
		return param1 == null || param1.isBlank() ;
	}
	
	private boolean checkIfParamsIsNotNull(String param1, String param2) {
		return param1 == null || param1.isBlank() ||
				param2 == null || param2.isBlank();
	}
	
	private boolean checkIfParamsIsNotNull(String param1, String param2, String param3) {
		return 	param1 == null || param1.isBlank() ||
				param2 == null || param2.isBlank() ||
				param3 == null || param3.isBlank();
	}
}
