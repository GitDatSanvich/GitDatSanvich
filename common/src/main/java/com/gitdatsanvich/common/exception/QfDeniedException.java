package com.gitdatsanvich.common.exception;

import lombok.NoArgsConstructor;

/**
 * @author pengzhen
 * @date 2020-06-06
 * 403 授权拒绝
 */
@NoArgsConstructor
public class QfDeniedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public QfDeniedException(String message) {
		super(message);
	}

	public QfDeniedException(Throwable cause) {
		super(cause);
	}

	public QfDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public QfDeniedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
