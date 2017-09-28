package com.ilscipio.scipio.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.party.content.PartyContentWrapper;
import org.ofbiz.party.party.PartyWorker;
import org.ofbiz.service.LocalDispatcher;

/**
 * Raw setup step data check logic. 
 * USE {@link SetupWorker} TO INVOKE THESE DURING REAL SETUP. 
 * This is for general reuse and to keep the core logic clear/separate.
 */
public abstract class SetupDataUtil {

    public static final String module = SetupDataUtil.class.getName();

    protected SetupDataUtil() {
    }

    /*
     * ******************************************* 
     * Setup step elemental data state queries 
     * *******************************************
     */

    // WARN: params map may contain unvalidated user input - others in the map may be already validated.
    // The caller (SetupWorker.CommonStepState subclasses) handles the implicit deps and decides which params must be pre-validated.
    // DO NOT call these methods from screen - all must go through SetupWorker.

    public static Map<String, Object> getOrganizationStepData(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> params, boolean useCache)
            throws GeneralException {
        Map<String, Object> result = UtilMisc.toMap("completed", false);

        String orgPartyId = (String) params.get("orgPartyId");

        if (UtilValidate.isNotEmpty(orgPartyId)) {
            Map<String, Object> fields = new HashMap<>();
            fields.put("roleTypeId", "INTERNAL_ORGANIZATIO");
            fields.put("partyId", orgPartyId);
            List<GenericValue> partyRoles = delegator.findByAnd("PartyRole", fields, null, useCache);
            if (UtilValidate.isNotEmpty(partyRoles)) {
                result.put("partyValid", true);
                result.put("orgPartyId", orgPartyId);
                result.put("completed", true);
            } else {
                result.put("partyValid", false);
            }
        }
        return result;
    }

    public static Map<String, Object> getUserStepData(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> params, boolean useCache)
            throws GeneralException {
        Map<String, Object> result = UtilMisc.toMap("completed", false);

        String orgPartyId = (String) params.get("orgPartyId");        
        String userPartyId = (String) params.get("userPartyId");
        
        GenericValue party = null;        
        if (UtilValidate.isNotEmpty(orgPartyId)) {
            if (UtilValidate.isNotEmpty(userPartyId)) {                
                party = delegator.findOne("Party", UtilMisc.toMap("partyId", userPartyId), useCache);
                if (party != null) {
                    List<GenericValue> partyRelationshipOwnerList = party.getRelated("ToPartyRelationship",
                            UtilMisc.toMap("partyIdFrom", orgPartyId, "roleTypeIdFrom", "INTERNAL_ORGANIZATIO", "partyRelationshipTypeId", "OWNER"),
                            UtilMisc.toList("fromDate DESC"), false);
                    if (UtilValidate.isNotEmpty(partyRelationshipOwnerList)) {
                        if (partyRelationshipOwnerList.size() > 1) {
                            Debug.logWarning("Setup: User " + userPartyId + "' got multiple owner relationships for organization '" 
                                    + orgPartyId + "'", module);
                        }
                    } else {
                        Debug.logError("Setup: User '" + userPartyId + "'" + " is not an owner of organization '" + orgPartyId + "'; ignoring", module);
                        party = null;
                    }
                } else {
                    Debug.logError("Setup: User '" + userPartyId + "' not found; ignoring", module);
                }
            } else if (!UtilMisc.booleanValueVersatile(params.get("newUser"), false)) {
                GenericValue orgParty = delegator.findOne("Party", UtilMisc.toMap("partyId", orgPartyId), useCache);
                if (orgParty != null) {
                    List<GenericValue> partyRelationshipOwnerList = orgParty.getRelated("FromPartyRelationship",
                            UtilMisc.toMap("partyIdFrom", orgPartyId, "roleTypeIdFrom", "INTERNAL_ORGANIZATIO", "partyRelationshipTypeId", "OWNER"),
                            UtilMisc.toList("fromDate DESC"), false);
                    if (UtilValidate.isNotEmpty(partyRelationshipOwnerList)) {
                        if (partyRelationshipOwnerList.size() > 1) {
                           Debug.logWarning("Setup: User " + userPartyId + "' got multiple owner relationships for organization '" 
                                    + orgPartyId + "'", module);
                        }
                        GenericValue partyRelationshipOwner = EntityUtil.getFirst(partyRelationshipOwnerList);
                        party = partyRelationshipOwner.getRelatedOne("ToParty", false);
                    }
                }
            }
        }
        if (party != null) {
            GenericValue userUserLogin = EntityUtil.getFirst(party.getRelated("UserLogin", UtilMisc.toMap("partyId", userPartyId), null, false));
            GenericValue userPerson = party.getRelatedOne("Person", false);
            GenericValue postalAddress = PartyWorker.findPartyLatestPostalAddress(userPartyId, delegator);
            GenericValue emailAddress = PartyWorker.findPartyLatestContactMech(userPartyId, "EMAIL_ADDRESS", delegator);
            GenericValue telecomNumber = PartyWorker.findPartyLatestContactMech(userPartyId, "TELECOM_NUMBER", delegator);

            result.put("userUserLogin", userUserLogin);
            result.put("userPerson", userPerson);
            result.put("userPostalAddress", postalAddress);
            result.put("userEmailAddress", emailAddress);
            result.put("userTelecomNumber", telecomNumber);

            List<GenericValue> userPartyContactMechList = delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", userPartyId), null, false);
            
            result.put("userPartyId", userPartyId);
            result.put("userParty", party);
            result.put("completed", true);       
        }
        return result;
    }

    public static Map<String, Object> getAccountingStepData(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> params, boolean useCache)
            throws GeneralException {
        Map<String, Object> result = UtilMisc.toMap("completed", false);

        // TODO

        return result;
    }

    public static Map<String, Object> getFacilityStepData(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> params, boolean useCache)
            throws GeneralException {
        Map<String, Object> result = UtilMisc.toMap("completed", false);

        String facilityId = (String) params.get("facilityId");
        String orgPartyId = (String) params.get("orgPartyId");
        String productStoreId = (String) params.get("productStoreId");

        GenericValue facility = null;
        if (UtilValidate.isNotEmpty(orgPartyId)) {
            if (UtilValidate.isNotEmpty(facilityId)) {
                // filter by owner to prevent editing other companies's facilities
                facility = delegator.findOne("Facility", UtilMisc.toMap("facilityId", facilityId), useCache);
                if (facility != null) {
                    if (orgPartyId.equals(facility.getString("ownerPartyId"))) {
                        ;
                    } else {
                        Debug.logError("Setup: Facility '" + facilityId + "' does not belong to organization '" + orgPartyId + "'; ignoring", module);
                        facility = null;
                    }
                } else {
                    Debug.logError("Setup: Facility '" + facilityId + "' not found; ignoring", module);
                }
            } else if (!UtilMisc.booleanValueVersatile(params.get("newFacility"), false)) {
                if (UtilValidate.isNotEmpty(productStoreId)) {
                    // this case selects the best facility for the passed store
                    // TODO: REVIEW: this is not reusing the getStoreStepStateData for now because
                    // facility step now comes first and will create endless loop 
                    GenericValue productStore = delegator.findOne("ProductStore", UtilMisc.toMap("productStoreId", productStoreId), useCache);
                    if (productStore != null) {
                        if (orgPartyId.equals(productStore.getString("payToPartyId"))) {
                            facilityId = productStore.getString("inventoryFacilityId");
                            if (UtilValidate.isNotEmpty(facilityId)) {
                                facility = delegator.findOne("Facility", UtilMisc.toMap("facilityId", facilityId), useCache);
                                if (facility != null) {
                                    if (orgPartyId.equals(facility.getString("ownerPartyId"))) {
                                        result.put("facilityId", facility.getString("facilityId"));
                                        result.put("facility", facility);
                                        result.put("completed", true);
                                        return result;
                                    } else {
                                        Debug.logError("Setup: Warehouse '" + facilityId + "'" 
                                                + " does not belong to organization '" 
                                                + orgPartyId + "'; ignoring", module);
                                        facility = null;
                                    }
                                } else {
                                    Debug.logError("Setup: Warehouse '" + facilityId + "' not found; ignoring", module);
                                }
                            } else {
                                // TODO: REVIEW: there are multiple reasons for this;
                                // * does not support ProductStoreFacility-only or multi-facility for now;
                                // * product store was created without a facility
                                Debug.logWarning("Setup: Cannot get warehouse for store '" 
                                        + productStoreId + "'" + " because ProductStore.inventoryFacilityId is not set", module);
                            }
                        } else {
                            Debug.logError("Setup: ProductStore '" + productStoreId + "' does not appear to belong to"
                                    + " organization '" + orgPartyId + "'; ignoring", module);
                            productStore = null;
                        }
                    } else {
                        Debug.logError("Setup: ProductStore '" + productStoreId + "' not found; ignoring", module);
                    }
                } else {
                    List<GenericValue> facilities = delegator.findByAnd("Facility", UtilMisc.toMap("ownerPartyId", orgPartyId), null, useCache);
                    facility = EntityUtil.getFirst(facilities);
                    if (facilities.size() >= 2) {
                        Debug.logInfo("Setup: Multiple warehouses found for organization '" + orgPartyId 
                                + "'; selecting first ('" + facility.getString("facilityId") + "')", module);
                    }
                }
            }
        }
        if (facility != null) {
            facilityId = facility.getString("facilityId");
            result.put("facilityId", facilityId);
            result.put("facility", facility);

            Map<String, Object> fields = UtilMisc.toMap("facilityId", facilityId);
            List<GenericValue> contactMechPurposes = EntityUtil.filterByDate(delegator.findByAnd("FacilityContactMechPurpose", 
                    fields, UtilMisc.toList("-fromDate"), useCache));
            result.put("facilityContactMechPurposeList", contactMechPurposes);

            Set<String> purposeTypes = getEntityStringFieldValues(contactMechPurposes, "contactMechPurposeTypeId", new HashSet<String>());
            boolean completed = true;
            if (!purposeTypes.contains("SHIPPING_LOCATION")) {
                Debug.logInfo("Setup: Warehouse '" + facilityId + "' has no SHIPPING_LOCATION contact mech; treating as incomplete", module);
                completed = false;
            }
            if (!purposeTypes.contains("SHIP_ORIG_LOCATION")) {
                Debug.logInfo("Setup: Warehouse '" + facilityId + "' has no SHIP_ORIG_LOCATION contact mech; treating as incomplete", module);
                completed = false;
            }
            result.put("completed", completed);
        }
        return result;
    }

    public static Map<String, Object> getCatalogStepStateData(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> params, boolean useCache)
            throws GeneralException {
        Map<String, Object> result = UtilMisc.toMap("completed", false);

        String productStoreId = (String) params.get("productStoreId");
        String prodCatalogId = (String) params.get("prodCatalogId");

        boolean specCatalog = false;

        Map<String, Object> fields = UtilMisc.toMap("productStoreId", productStoreId);
        if (UtilValidate.isNotEmpty(prodCatalogId)) {
            fields.put("prodCatalogId", prodCatalogId);
            specCatalog = true;
        }
        List<GenericValue> productStoreCatalogList = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreCatalog", 
                fields, UtilMisc.toList("sequenceNum ASC"), useCache));
        result.put("productStoreCatalogList", productStoreCatalogList);

        GenericValue productStoreCatalog = EntityUtil.getFirst(productStoreCatalogList);
        if (productStoreCatalog != null) {
            prodCatalogId = productStoreCatalog.getString("prodCatalogId");

            if (!specCatalog && productStoreCatalogList.size() >= 2) {
                Debug.logInfo("Setup: Store '" + productStoreId + "' has multiple active catalogs, selecting first ('" + prodCatalogId + "') for setup"
                        + " (catalogs: " + getEntityStringFieldValues(productStoreCatalogList, "prodCatalogId", new ArrayList<String>(productStoreCatalogList.size())) + ")",
                        prodCatalogId);
            }

            GenericValue prodCatalog = productStoreCatalog.getRelatedOne("ProdCatalog", useCache);

            result.put("productStoreCatalog", productStoreCatalog);
            result.put("prodCatalog", prodCatalog);
            result.put("completed", true);
        }
        return result;
    }

    public static Map<String, Object> getStoreStepStateData(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> params, boolean useCache)
            throws GeneralException {
        Map<String, Object> result = UtilMisc.toMap("completed", false);

        String productStoreId = (String) params.get("productStoreId");
        String orgPartyId = (String) params.get("orgPartyId");

        GenericValue productStore = null;
        if (UtilValidate.isNotEmpty(productStoreId)) {
            if (UtilValidate.isNotEmpty(orgPartyId)) {
                Map<String, Object> fields = UtilMisc.toMap("productStoreId", productStoreId, "payToPartyId", orgPartyId);
                List<GenericValue> productStores = delegator.findByAnd("ProductStore", fields, null, useCache);
                if (UtilValidate.isNotEmpty(productStores)) {
                    productStore = productStores.get(0);
                }
            } else {
                // we'll require a non-null orgPartyId here to simplify, so both parameters should be passed around
            }
        } else if (!UtilMisc.booleanValueVersatile(params.get("newStore"), false)) {
            // Unless asked to create a new store, read the first store by default;
            // in majority cases clients will create one store per company, so this saves some reloading.
            if (UtilValidate.isNotEmpty(orgPartyId)) {
                Map<String, Object> fields = UtilMisc.toMap("payToPartyId", orgPartyId);
                List<GenericValue> productStores = delegator.findByAnd("ProductStore", fields, null, useCache);
                if (UtilValidate.isNotEmpty(productStores)) {
                    productStore = productStores.get(0);
                    if (productStores.size() >= 2) {
                        Debug.logInfo("Setup: Organization '" + orgPartyId 
                            + "' has multiple ProductStores (" + productStores.size()
                            + "); auto-selecting first for the setup process (productStoreId: "
                            + productStore.getString("productStoreId") + ")", module);
                    }
                }
            }
        }

        if (productStore != null) {
            productStoreId = productStore.getString("productStoreId");
            result.put("productStoreId", productStoreId);
            result.put("productStore", productStore);

            String facilityId = productStore.getString("inventoryFacilityId");
            if (UtilValidate.isNotEmpty(facilityId)) {
                Map<String, Object> fields = UtilMisc.toMap("productStoreId", productStoreId, "facilityId", facilityId);
                List<GenericValue> productFacilityList = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreFacility", 
                        fields, UtilMisc.toList("sequenceNum ASC"), useCache));
                if (UtilValidate.isNotEmpty(productFacilityList)) {
                    result.put("completed", true);
                } else {
                    Debug.logWarning("Setup: ProductStore '" + productStoreId + "' has no ProductStoreFacility relation for warehouse '" + facilityId
                            + "'; treating store as incomplete" + " (NOTE: may require manually fixing the schema)", module);
                }
            } else {
                Debug.logWarning("Setup: ProductStore '" + productStoreId + "' has no inventoryFacilityId field; treating store as incomplete", module);
            }
            result.put("facilityId", facilityId);
        }
        return result;
    }

    public static Map<String, Object> getWebsiteStepStateData(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> params, boolean useCache)
            throws GeneralException {
        Map<String, Object> result = UtilMisc.toMap("completed", false);

        String productStoreId = (String) params.get("productStoreId");

        Map<String, Object> fields = UtilMisc.toMap("productStoreId", productStoreId);
        GenericValue webSite = getFirstMaxOneExpected(delegator.findByAnd("WebSite", fields, null, useCache), fields);
        if (webSite != null) {
            result.put("webSiteId", webSite.getString("webSiteId"));
            result.put("webSite", webSite);
            result.put("completed", true);
        }
        return result;
    }

    /*
     * ******************************************* 
     * Generic helpers
     * *******************************************
     */

    private static GenericValue getFirstMaxOneExpected(List<GenericValue> values, Object query) {
        GenericValue value = EntityUtil.getFirst(values);
        if (values != null && values.size() >= 2) {
            // essential for debugging
            Debug.logWarning("Setup: Expected one " + value.getEntityName() 
                + " record at most, but found " + values.size() + " records matching for query: "
                + query + "; using first only (" + value.getPkShortValueString() + ")", module);
        }
        return value;
    }

    private static <T extends Collection<String>> T getEntityStringFieldValues(List<GenericValue> values, String fieldName, T out) {
        for (GenericValue value : values) {
            String str = value.getString(fieldName);
            if (str != null)
                out.add(str);
        }
        return out;
    }
}
