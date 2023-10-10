package com.example.usersservices_mychatserver.service;

import com.example.usersservices_mychatserver.entity.response.Status;
import com.example.usersservices_mychatserver.model.CodeVerification;
import com.example.usersservices_mychatserver.entity.response.Result;
import com.example.usersservices_mychatserver.port.in.ActivateUserAccountUseCase;
import com.example.usersservices_mychatserver.port.out.persistence.CodeVerificationRepositoryPort;
import com.example.usersservices_mychatserver.port.out.persistence.UserRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ActiveUserAccountService implements ActivateUserAccountUseCase {
    private final CodeVerificationRepositoryPort postgreCodeVerificationRepository;
    private final UserRepositoryPort userRepository;

    private static final String CODE_NOT_FOUND_FOR_THIS_USER = "Code not found for this user";
    private static final String BAD_CODE = "Bad code";

    public ActiveUserAccountService(CodeVerificationRepositoryPort postgreCodeVerificationRepository, UserRepositoryPort userRepository) {
        this.postgreCodeVerificationRepository = postgreCodeVerificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Result<Status>> activateUserAccount(Mono<CodeVerification> codeVerificationMono) {
          return codeVerificationMono.flatMap(
                codeVerificationProvidedByUser -> postgreCodeVerificationRepository.findUserActiveAccountCodeById(codeVerificationProvidedByUser.idUser()).flatMap(
                        codeVerificationSaved -> {
                            if(codeVerificationSaved.code().equals(codeVerificationProvidedByUser.code())){
                                return userRepository.activeUserAccount(codeVerificationProvidedByUser.idUser()).
                                        then(Mono.defer(() -> postgreCodeVerificationRepository.deleteUserActivationCode(codeVerificationSaved).
                                                thenReturn(Result.success(new Status(true)))));
                            }else{
                                return Mono.just(Result.<Status>error(BAD_CODE));
                            }
                        }
                ).switchIfEmpty(Mono.just(Result.error(CODE_NOT_FOUND_FOR_THIS_USER)))

        );

    }
}
