package com.tools.monitor.exception;

import com.tools.monitor.entity.ResponseVO;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * GlobalExceptionHandler
 *
 * @author liu
 * @since 2022-10-01
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseVO globalException(Exception exception) {
		logger.info(exception.getMessage() + " : " + new Date() + "......", exception);
		return ResponseVO.exceptionResponseVO(exception);
	}
	@ExceptionHandler(ParamsException.class)
	@ResponseBody
	public ResponseVO paramsExceptionHandler(ParamsException e){
		if(e.getMsg().equals("无法生成指标")){
			return ResponseVO.errorTarget(e.getMsg());
		}
		return ResponseVO.errorResponseVO(e.getMsg());
	}

	@ExceptionHandler({ConstraintViolationException.class, BindException.class})
	public ResponseVO exceptionHandler(Exception ex, HttpServletRequest request) {
		ex.printStackTrace();
		String msg = "";
		if (ex instanceof ConstraintViolationException) {
			ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex;
			ConstraintViolation<?> next = constraintViolationException.getConstraintViolations().iterator().next();
			msg = next.getMessage();
		} else if (ex instanceof BindException) {
			BindException bindException = (BindException) ex;
			msg = bindException.getBindingResult().getFieldError().getDefaultMessage();
		}
		return ResponseVO.errorResponseVO(msg);
	}
}
