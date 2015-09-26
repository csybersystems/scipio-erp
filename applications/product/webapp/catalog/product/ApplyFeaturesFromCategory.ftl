<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<@menu type="button">
<#if curProductFeatureCategory??>
  <@menuitem type="link" ofbizHref="EditFeature?productFeatureCategoryId=${productFeatureCategoryId!}" text="${uiLabelMap.ProductCreateNewFeature}" />
<#elseif productFeatureGroup??>
  <@menuitem type="link" ofbizHref="EditFeatureGroupAppls?productFeatureGroupId=${productFeatureGroup.productFeatureGroupId!}" text="${uiLabelMap.CommonEdit} ${productFeatureGroup.description!}" />
</#if>
<#if productId?has_content>
  <@menuitem type="link" ofbizHref="EditProduct?productId=${productId}" text="${uiLabelMap.ProductReturnToEditProduct}" />
  <@menuitem type="link" ofbizHref="EditProductFeatures?productId=${productId}" text="${uiLabelMap.ProductReturnToEditProductFeatures}" />
</#if>
</@menu>

<#if (listSize > 0)>
  <#assign selectedFeatureApplTypeId = selFeatureApplTypeId!>
  <#if productId?has_content>
    <#assign productString = "&amp;productId=" + productId>
  </#if>
    
  <@menu type="button">
    <@menuitem type="link" ofbizHref="ApplyFeaturesFromCategory?productFeatureCategoryId=${productFeatureCategoryId!}&amp;productFeatureApplTypeId=${selectedFeatureApplTypeId!}&amp;VIEW_SIZE=${viewSize}&amp;VIEW_INDEX=${viewIndex-1}${productString!}" text="[${uiLabelMap.CommonPrevious}]" disabled=(!(viewIndex > 0)) />
    <@menuitem type="text" text="${lowIndex+1} - ${highIndex} ${uiLabelMap.CommonOf} ${listSize}" />
    <@menuitem type="link" ofbizHref="ApplyFeaturesFromCategory?productFeatureCategoryId=${productFeatureCategoryId!}&amp;productFeatureApplTypeId=${selectedFeatureApplTypeId!}&amp;VIEW_SIZE=${viewSize}&amp;VIEW_INDEX=${viewIndex+1}${productString!}" text="[${uiLabelMap.CommonNext}]" disabled=(!(listSize > highIndex)) />
  </@menu>
</#if>

<form method="post" action="<@ofbizUrl>ApplyFeaturesToProduct</@ofbizUrl>" name="selectAllForm">
  <input type="hidden" name="_useRowSubmit" value="Y" />
  <input type="hidden" name="_checkGlobalScope" value="Y" />
  <input type="hidden" name="productId" value="${productId}" />
  
<@table type="data-list" autoAltRows=true cellspacing="0" class="basic-table">
  <@thead>
  <@tr class="header-row">
    <@th>${uiLabelMap.CommonId}</@th>
    <@th>${uiLabelMap.CommonDescription}</@th>
    <@th>${uiLabelMap.ProductFeatureType}</@th>
    <@th>${uiLabelMap.ProductApplType}</@th>
    <@th>${uiLabelMap.CommonFromDate}</@th>
    <@th>${uiLabelMap.CommonThruDate}</@th>
    <@th>${uiLabelMap.ProductAmount}</@th>
    <@th>${uiLabelMap.CommonSequence}</@th>
    <@th>${uiLabelMap.CommonAll}<input type="checkbox" name="selectAll" value="${uiLabelMap.CommonY}" onclick="javascript:toggleAll(this, 'selectAllForm');highlightAllRows(this, 'productFeatureId_tableRow_', 'selectAllForm');" /></@th>
  </@tr>
  </@thead>
<#assign rowCount = 0>
<#if (listSize > 0)>
<#list productFeatures as productFeature>
  <#assign curProductFeatureType = productFeature.getRelatedOne("ProductFeatureType", true)>
    <@tr id="productFeatureId_tableRow_${rowCount}" valign="middle">
        <input type="hidden" name="productFeatureId_o_${rowCount}" value="${productFeature.productFeatureId}" />
        <@td><a href="<@ofbizUrl>EditFeature?productFeatureId=${productFeature.productFeatureId}</@ofbizUrl>" class="${styles.button_default!}">${productFeature.productFeatureId}</a></@td>
        <@td>${productFeature.description!}</@td>
        <@td><#if curProductFeatureType??>${curProductFeatureType.description!}<#else> [${productFeature.productFeatureTypeId}]</#if></@td>
        <@td>
          <select name="productFeatureApplTypeId_o_${rowCount}" size="1">
            <#list productFeatureApplTypes as productFeatureApplType>
              <option value="${productFeatureApplType.productFeatureApplTypeId}" <#if (selectedFeatureApplTypeId?has_content) && (productFeatureApplType.productFeatureApplTypeId == selectedFeatureApplTypeId)>selected="selected"</#if>>${productFeatureApplType.get("description", locale)}</option>
            </#list>
          </select>
        </@td>
        <@td>
            <@htmlTemplate.renderDateTimeField name="fromDate_o_${rowCount}" event="" action="" className=""  title="Format: yyyy-MM-dd HH:mm:ss.SSS" value="" size="25" maxlength="30" id="fromDate_o_${rowCount}" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
        </@td>
        <@td>
           <@htmlTemplate.renderDateTimeField name="thruDate_o_${rowCount}" event="" action="" className=""  title="Format: yyyy-MM-dd HH:mm:ss.SSS" value="" size="25" maxlength="30" id="thruDate_o_${rowCount}" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
        </@td>
        <@td><input type="text" size="6" name="amount_o_${rowCount}" value="${productFeature.defaultAmount!}" /></@td>
        <@td><input type="text" size="5" name="sequenceNum_o_${rowCount}" value="${productFeature.defaultSequenceNum!}" /></@td>
        <@td align="right">
            <input type="checkbox" name="_rowSubmit_o_${rowCount}" value="Y" onclick="javascript:checkToggle(this, 'selectAllForm');highlightRow(this,'productFeatureId_tableRow_${rowCount}');" />
        </@td>
    </@tr>
    <#assign rowCount = rowCount + 1>
</#list>
  <@tfoot>
    <@tr><@td colspan="9" align="center"><input type="submit" value="${uiLabelMap.CommonApply}" /></@td></@tr>
  </@tfoot>
</#if>
</@table>

  <input type="hidden" name="_rowCount" value="${rowCount!}"/>
</form>
