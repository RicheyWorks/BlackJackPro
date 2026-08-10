package com.richeyworks.blackjack.platform.compliance;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicensingPolicyTest {

    @Test
    void cryptoFailsClosedForUnlicensedAndUnknownStates() {
        LicensingPolicy p = LicensingPolicy.usDefault();
        assertFalse(p.cryptoAllowed("TX"), "unlicensed state must not allow crypto");
        assertFalse(p.cryptoAllowed(null));
        assertFalse(p.cryptoAllowed(""));
        assertFalse(p.cryptoAllowed("CA"), "prohibited");
        assertTrue(p.cryptoAllowed("NJ"), "licensed and not prohibited");
    }

    @Test
    void cryptoRequiresLicenseEvenIfNotOnBanList() {
        LicensingPolicy p = new LicensingPolicy(Set.of("NJ"), Set.of());
        assertFalse(p.cryptoAllowed("NY"));
        assertTrue(p.cryptoAllowed("NJ"));
    }

    @Test
    void stateCodesAreCaseInsensitive() {
        LicensingPolicy p = LicensingPolicy.usDefault();
        assertTrue(p.isLicensed("nj"));
        assertTrue(p.isLicensed("Nj"));
        assertTrue(p.cryptoAllowed("nj"));
    }

    @Test
    void maineNotYetOperational() {
        assertFalse(LicensingPolicy.usDefault().isLicensed("ME"),
                "ME is legalized but not accepting play in the default snapshot");
    }
}
