package mx.com.gunix.framework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.gunix.framework.token.persistence.TokenMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class TokenServiceImpl implements TokenService {
	@Autowired
	TokenMapper tm;

	@Override
	public void registraToken(String token) {
		tm.insertaToken(token);
	}

	@Override
	public void liberaToken(String token) {
		tm.finalizaToken(token);
	}

	@Override
	public Boolean tokenActivo(String token) {
		return tm.isTokenActivo(token);
	}

	@Override
	public void eliminaToken(String token) {
		tm.eliminaToken(token);
	}

}
