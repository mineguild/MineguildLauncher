package net.mineguild.Launcher.minecraft;

import javax.swing.SwingWorker;

import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class LoginWorker extends SwingWorker<LoginResponse, Void> {

  Exception t = null;
  boolean success = true;
  YggdrasilUserAuthentication auth;

  public LoginWorker(YggdrasilUserAuthentication auth) {
    this.auth = auth;
  }

  @Override
  protected LoginResponse doInBackground() throws Exception {
    try {
      auth.logIn();
      if (auth.isLoggedIn() && auth.canPlayOnline()) {
        int result = LoginDialog.mg_login(auth.getSelectedProfile().getId().toString());
        if (result < 1) {
          throw new MGAuthException("Not whitelisted!");
        }

        LoginResponse response =
            new LoginResponse(Integer.toString(auth.getAgent().getVersion()), "token", auth
                .getSelectedProfile().getName(), null,
                auth.getSelectedProfile().getId().toString(), auth);
        return response;
      }
    } catch (Exception t) {
      this.t = t;
      success = false;
    }
    return null;
  }

  @Override
  protected void done() {
    firePropertyChange("done", null, true);
  }
}
