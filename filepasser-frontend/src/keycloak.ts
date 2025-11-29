import Keycloak from "keycloak-js";

type KeycloakConfig = {
  url: string;
  realm: string;
  clientId: string;
};

const config: KeycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8085",
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? "filepasser",
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "filepasser-frontend",
};

const keycloak = new Keycloak(config);

export default keycloak;
