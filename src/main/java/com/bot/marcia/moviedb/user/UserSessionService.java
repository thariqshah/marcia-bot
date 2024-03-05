package com.bot.marcia.moviedb.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    public UserSession getLoggedInUser(String chatId){
       return userSessionRepository.findById(chatId).orElseThrow(NoUserException::new);
    }


    public void save(String chatId,String username,String session,String requestToken, String accessToken,Integer accountId, String accountObjectId) {
        UserSession userSession = new UserSession();
        userSession.setChatId(chatId);
        userSession.setUsername(username);
        userSession.setSessionId(session);
        userSession.setRequestToken(requestToken);
        userSession.setAccessToken(accessToken);
        userSession.setAccountId(accountId);
        userSession.setAccountObjectId(accountObjectId);
        userSessionRepository.save(userSession);
    }
}
