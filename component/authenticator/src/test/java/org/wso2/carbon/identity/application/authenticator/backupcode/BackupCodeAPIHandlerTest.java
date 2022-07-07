package org.wso2.carbon.identity.application.authenticator.backupcode;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authenticator.backupcode.exception.BackupCodeException;
import org.wso2.carbon.identity.application.authenticator.backupcode.util.BackupCodeUtil;

import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.wso2.carbon.identity.application.authenticator.backupcode.constants.BackupCodeAuthenticatorConstants.Claims.BACKUP_CODES_CLAIM;
import static org.wso2.carbon.identity.application.authenticator.backupcode.constants.BackupCodeAuthenticatorConstants.Claims.BACKUP_CODES_ENABLED_CLAIM;

@PrepareForTest({BackupCodeAPIHandler.class, BackupCodeUtil.class, MultitenantUtils.class})
public class BackupCodeAPIHandlerTest extends PowerMockTestCase {

    private String username = "test1";
    private String tenantAwareUserName = "test1";
    private String tenantDomain = "test.domain";

    @Mock
    UserRealm userRealm;

    @Mock
    UserStoreManager userStoreManager;

    @Test(dataProvider = "backupCodesCountData")
    public void testGetRemainingBackupCodesCount(Map<String, String> userClaimValues, int remainingBackupCodesCount)
            throws UserStoreException, BackupCodeException {

        mockStatic(BackupCodeUtil.class);
        mockStatic(MultitenantUtils.class);
        BackupCodeAPIHandler backupCodeAPIHandler = new BackupCodeAPIHandler();

        when(BackupCodeUtil.getUserRealm(username)).thenReturn(userRealm);
        when(MultitenantUtils.getTenantAwareUsername(username)).thenReturn(tenantAwareUserName);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(userStoreManager.getUserClaimValues(tenantAwareUserName, new String[]{BACKUP_CODES_CLAIM}, null)).
                thenReturn(userClaimValues);
        assertEquals(remainingBackupCodesCount, backupCodeAPIHandler.getRemainingBackupCodesCount(username));

        when(BackupCodeUtil.getUserRealm("test2")).thenReturn(null);
        assertEquals(0, backupCodeAPIHandler.getRemainingBackupCodesCount("test2"));
    }

    @DataProvider(name = "backupCodesCountData")
    public Object[][] dataForRemainingBackupCodesCount() {

        Map<String, String> testClaims1 = new HashMap<>();
        testClaims1.put(BACKUP_CODES_CLAIM, "");
        testClaims1.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        Map<String, String> testClaims2 = new HashMap<>();
        testClaims2.put(BACKUP_CODES_CLAIM, "234563");
        testClaims2.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        Map<String, String> testClaims3 = new HashMap<>();
        testClaims3.put(BACKUP_CODES_CLAIM, null);
        testClaims3.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        Map<String, String> testClaims4 = new HashMap<>();
        testClaims4.put(BACKUP_CODES_CLAIM, "234563,467064");
        testClaims4.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        return new Object[][]{
                {testClaims1, 0},
                {testClaims2, 1},
                {testClaims3, 0},
                {testClaims4, 2}
        };
    }

    @Test(dataProvider = "generateBackupCodesData")
    public void testGenerateBackupCodes(List<String> backupCodes) throws UserStoreException, BackupCodeException {

        mockStatic(BackupCodeUtil.class);
        mockStatic(MultitenantUtils.class);
        BackupCodeAPIHandler backupCodeAPIHandler = new BackupCodeAPIHandler();
        when(BackupCodeUtil.getUserRealm(username)).thenReturn(userRealm);
        when(MultitenantUtils.getTenantDomain(username)).thenReturn(tenantDomain);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(BackupCodeUtil.generateBackupCodes(tenantDomain)).thenReturn(backupCodes);
        assertEquals(backupCodes, backupCodeAPIHandler.generateBackupCodes(username));

        when(BackupCodeUtil.getUserRealm("test2")).thenReturn(null);
        assertEquals(new ArrayList<>(), backupCodeAPIHandler.generateBackupCodes("test2"));
    }

    @DataProvider(name = "generateBackupCodesData")
    public Object[][] dataForGeneratingBackupCodes(){

        List<String> backupCodes1 = new ArrayList<>();

        List<String> backupCodes2 = new ArrayList<>();
        backupCodes2.add("123567");
        backupCodes2.add("456789");

        List<String> backupCodes3 = new ArrayList<>();
        backupCodes3.add("");
        backupCodes3.add(" ");
        backupCodes3.add(null);

        return new Object[][]{
                {backupCodes1},
                {backupCodes2},
                {backupCodes3}
        };
    }

    @Test
    public void testDeleteBackupCodes() throws UserStoreException, BackupCodeException {

        mockStatic(BackupCodeUtil.class);
        mockStatic(MultitenantUtils.class);
        BackupCodeAPIHandler backupCodeAPIHandler = new BackupCodeAPIHandler();

        when(BackupCodeUtil.getUserRealm(username)).thenReturn(userRealm);
        when(MultitenantUtils.getTenantAwareUsername(username)).thenReturn(tenantAwareUserName);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        assertEquals(true, backupCodeAPIHandler.deleteBackupCodes(username));
    }
}
