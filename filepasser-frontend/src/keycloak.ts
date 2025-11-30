import Keycloak from "keycloak-js";

type KeycloakConfig = {
  url: string;
  realm: string;
  clientId: string;
};

const env = import.meta.env;

const config: KeycloakConfig = {
  url: env.VITE_KEYCLOAK_URL ?? "http://keycloak:8085",
  realm: env.VITE_KEYCLOAK_REALM ?? "filepasser",
  clientId: env.VITE_KEYCLOAK_CLIENT_ID ?? "filepasser-frontend",
};

const keycloak = new Keycloak(config);

export default keycloak;
