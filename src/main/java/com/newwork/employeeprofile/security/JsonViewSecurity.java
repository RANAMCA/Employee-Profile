package com.newwork.employeeprofile.security;

public class JsonViewSecurity {

  public interface PublicView {}
  public interface PrivateView extends PublicView {} // For employee themselves
  public interface ManagerView extends PrivateView {} // For manager (might add more fields)

  // You can also create a combined view
  public interface OwnerOrManagerView extends PrivateView {}
}
