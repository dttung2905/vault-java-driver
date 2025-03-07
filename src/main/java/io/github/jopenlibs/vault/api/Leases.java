package io.github.jopenlibs.vault.api;

import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.json.Json;
import io.github.jopenlibs.vault.response.VaultResponse;
import io.github.jopenlibs.vault.rest.Rest;
import io.github.jopenlibs.vault.rest.RestResponse;
import java.nio.charset.StandardCharsets;


/**
 * <p>The implementing class for operations on REST endpoints, under the "Leases" section of the
 * Vault HTTP API docs (<a href="https://www.vaultproject.io/docs/http/index.html">
 * https://www.vaultproject.io/docs/http/index.html</a>).</p>
 *
 * <p>This class is not intended to be constructed directly.  Rather, it is meant to used by way of
 * <code>Vault</code> in a DSL-style builder pattern.  See the Javadoc comments of each
 * <code>public</code>
 * method for usage examples.</p>
 */
public class Leases extends OperationsBase {

    private String nameSpace;

    public Leases(final VaultConfig config) {
        super(config);

        if (this.config.getNameSpace() != null && !this.config.getNameSpace().isEmpty()) {
            this.nameSpace = this.config.getNameSpace();
        }
    }

    public Leases withNameSpace(final String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    /**
     * <p>Immediately revokes a secret associated with a given lease.  E.g.:</p>
     *
     * <blockquote>
     * <pre>{@code
     * final VaultResponse response = vault.leases().revoke("7c63da27-a56b-3e3b-377d-ef74630a6d0b");
     * assertEquals(204, response.getRestResponse().getStatus());
     * }</pre>
     * </blockquote>
     *
     * @param leaseId A lease ID associated with the secret to be revoked
     * @return The response information returned from Vault
     * @throws VaultException If an error occurs, or unexpected reponse received from Vault
     */
    public VaultResponse revoke(final String leaseId) throws VaultException {
        return retry(attempt -> {
            final String requestJson = Json.object().add("lease_id", leaseId).toString();
            final RestResponse restResponse = new Rest()//NOPMD
                    .url(config.getAddress() + "/v1/sys/leases/revoke")
                    .header("X-Vault-Token", config.getToken())
                    .header("X-Vault-Namespace", this.nameSpace)
                    .header("X-Vault-Request", "true")
                    .body(requestJson.getBytes(StandardCharsets.UTF_8))
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout())
                    .sslVerification(config.getSslConfig().isVerify())
                    .sslContext(config.getSslConfig().getSslContext())
                    .post();

            // Validate response
            if (restResponse.getStatus() != 204) {
                throw new VaultException("Expecting HTTP status 204, but instead receiving "
                        + restResponse.getStatus(), restResponse.getStatus());
            }

            return new VaultResponse(restResponse, attempt);
        });
    }

    /**
     * <p>Revokes all secrets (via a lease ID prefix) or tokens (via the tokens' path property)
     * generated under a given prefix immediately.  This requires sudo capability and access to it
     * should be tightly controlled as it can be used to revoke very large numbers of secrets/tokens
     * at once. E.g.:</p>
     *
     * <blockquote>
     * <pre>{@code
     * final VaultResponse response = vault.leases().revokePrefix("aws");
     * assertEquals(204, response.getRestResponse().getStatus());
     * }</pre>
     * </blockquote>
     *
     * @param prefix A Vault path prefix, for which all secrets beneath it should be revoked
     * @return The response information returned from Vault
     * @throws VaultException If an error occurs, or unexpected reponse received from Vault
     */
    public VaultResponse revokePrefix(final String prefix) throws VaultException {
        return retry(attempt -> {
            final RestResponse restResponse = new Rest()//NOPMD
                    .url(config.getAddress() + "/v1/sys/leases/revoke-prefix/" + prefix)
                    .header("X-Vault-Token", config.getToken())
                    .header("X-Vault-Namespace", this.nameSpace)
                    .header("X-Vault-Request", "true")
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout())
                    .sslVerification(config.getSslConfig().isVerify())
                    .sslContext(config.getSslConfig().getSslContext())
                    .post();

            // Validate response
            if (restResponse.getStatus() != 204) {
                throw new VaultException("Expecting HTTP status 204, but instead receiving "
                        + restResponse.getStatus(), restResponse.getStatus());
            }
            return new VaultResponse(restResponse, attempt);
        });
    }

    /**
     * <p>Revokes all secrets or tokens generated under a given prefix immediately. Unlike
     * revokePrefix(String), this method ignores backend errors encountered during revocation. This
     * is potentially very dangerous and should only be used in specific emergency situations where
     * errors in the backend or the connected backend service prevent normal revocation.  By
     * ignoring these errors, Vault abdicates responsibility for ensuring that the issued
     * credentials or secrets are properly revoked and/or cleaned up. Access to this endpoint should
     * be tightly controlled. E.g.:</p>
     *
     * <blockquote>
     * <pre>{@code
     * final VaultResponse response = vault.leases().revokePrefix("aws");
     * assertEquals(204, response.getRestResponse().getStatus());
     * }</pre>
     * </blockquote>
     *
     * @param prefix A Vault path prefix, for which all secrets beneath it should be revoked
     * @return The response information returned from Vault
     * @throws VaultException If an error occurs, or unexpected reponse received from Vault
     */
    public VaultResponse revokeForce(final String prefix) throws VaultException {
        return retry(attempt -> {
            final RestResponse restResponse = new Rest()//NOPMD
                    .url(config.getAddress() + "/v1/sys/leases/revoke-force/" + prefix)
                    .header("X-Vault-Token", config.getToken())
                    .header("X-Vault-Namespace", this.nameSpace)
                    .header("X-Vault-Request", "true")
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout())
                    .sslVerification(config.getSslConfig().isVerify())
                    .sslContext(config.getSslConfig().getSslContext())
                    .post();

            // Validate response
            if (restResponse.getStatus() != 204) {
                throw new VaultException("Expecting HTTP status 204, but instead receiving "
                        + restResponse.getStatus(), restResponse.getStatus());
            }

            return new VaultResponse(restResponse, attempt);
        });
    }

    /**
     * <p>Renews a given secret lease.</p>
     *
     * <blockquote>
     * <pre>{@code
     * final VaultResponse response = vault.leases().renew("mongodb/creds/myapp/cd7f9834-b870-9ebc-3da5-27bf9cdc42ad");
     * assertEquals(200, response.getRestResponse().getStatus());
     * }</pre>
     * </blockquote>
     *
     * @param leaseId A lease ID associated with a secret
     * @param increment A requested amount of time in seconds to extend the lease. This is
     * advisory.
     * @return The response information returned from Vault
     * @throws VaultException The response information returned from Vault
     */
    public VaultResponse renew(final String leaseId, final long increment) throws VaultException {

        // TODO:  Update the integration test suite to provide coverate for this
        //        The "generic" backend does not support support lease renewal.  The only other backend
        //        available when we were using Vault in "dev mode" was the "pki" backend, which does
        //        support renewal of credentials, etc.  But lease renewal in this context is talking about
        //        secrets.  Now that the integration tests use a "real" Vault instance hosted in a Docker
        //        container, we can revisit this.

        return retry(attempt -> {
            final String requestJson = Json.object().add("increment", increment).toString();
            final RestResponse restResponse = new Rest()//NOPMD
                    .url(config.getAddress() + "/v1/sys/leases/renew/" + leaseId)
                    .header("X-Vault-Token", config.getToken())
                    .header("X-Vault-Namespace", this.nameSpace)
                    .header("X-Vault-Request", "true")
                    .body(increment < 0 ? null : requestJson.getBytes(StandardCharsets.UTF_8))
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout())
                    .sslVerification(config.getSslConfig().isVerify())
                    .sslContext(config.getSslConfig().getSslContext())
                    .post();

            // Validate response
            if (restResponse.getStatus() != 200) {
                throw new VaultException("Expecting HTTP status 200, but instead receiving "
                        + restResponse.getStatus(), restResponse.getStatus());
            }

            return new VaultResponse(restResponse, attempt);
        });
    }
}
