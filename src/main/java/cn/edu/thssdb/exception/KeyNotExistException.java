package cn.edu.thssdb.exception;

public class KeyNotExistException extends RuntimeException{
  @Override
  public String getMessage() {
    return "Exception: key doesn't exist!";
  }
}
