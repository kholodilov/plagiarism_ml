<select name="minMatch">
<#list [8, 9, 10, 11, 12] as minMatch>
    <option <#if model.minMatch == minMatch>selected</#if>>${minMatch}</option>
</#list>
</select>