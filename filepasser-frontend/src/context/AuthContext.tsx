import React from "react";
import { ReactKeycloakProvider, useKeycloak } from "@react-keycloak/web";
import keycloak from "../keycloak";
import { setAuthToken } from "../api/http";

type AuthProviderProps = {
  children: React.ReactNode;
};

const onTokens = (tokens: { token?: string } | undefined) => {
  if (tokens?.token) {
    setAuthToken(tokens.token);
  } else {
    setAuthToken(undefined);
  }
};

export const AuthProvider = ({ children }: AuthProviderProps) => (
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={{ onLoad: "check-sso", redirectUri: window.location.origin + "/dashboard", silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`, checkLoginIframe: false }}
    onTokens={onTokens}
    LoadingComponent={null}
  >
    {children}
  </ReactKeycloakProvider>
);

export const useAuth = () => {
  const { keycloak, initialized } = useKeycloak();
  const contractId = keycloak.tokenParsed?.contractId as string | undefined;
  const email = keycloak.tokenParsed?.email as string | undefined;
  return {
    authenticated: keycloak.authenticated ?? false,
    initialized,
    contractId,
    email,
    login: (redirectUri?: string) => keycloak.login({ redirectUri: redirectUri ?? window.location.origin + "/" }),
    logout: () => keycloak.logout({ redirectUri: window.location.origin }),
  };
};
