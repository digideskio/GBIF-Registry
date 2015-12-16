<#include "../functions.ftl">
<#escape x as x?xml>
<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
    <dc:title>${dataset.title!}</dc:title>
    <dc:publisher>${(organization.title)!}</dc:publisher>

    <#-- Always use only one identifier -->
    <dc:identifier>${dc.identifier}</dc:identifier>
    <#if dataset.keywordCollections?has_content>
    <#list dataset.keywordCollections![] as kwc>
    <dc:subject>${kwc.keywords?join(", ")}</dc:subject>
    </#list>
    </#if>

    <dc:source>${dataset.homepage!}</dc:source>
    <#-- We should probably add a lang="en" attribute when the description was generated by GBIF -->
    <#list dc.description![] as description>
    <dc:description>${description}</dc:description>
    </#list>
    <#if dataset.purpose?has_content><dc:description>${dataset.purpose}</dc:description></#if>
    <@printIfHasContent dc.occurrenceCount!; oc><dc:description>${oc} ${fmUtil.choiceFormat("0#occurrences|1#occurrence|1<occurrences", dc.occurrenceCount)}</dc:description></@printIfHasContent>

    <dc:type>Dataset</dc:type>

    <#list dc.creators![] as creator>
    <dc:creator>${creator}</dc:creator>
    </#list>

    <#-- The date on which the resource was published. -->
    <#if dataset.pubDate?has_content>
    <dc:date>${isodate(dataset.pubDate)}</dc:date>
    </#if>
    <dc:language>${(dataset.dataLanguage.iso2LetterCode)!"en"}</dc:language>
    <@printIfHasContent dataset.rights!; rights><dc:rights>${rights}</dc:rights></@printIfHasContent>

    <#list dc.formattedTemporalCoverage![] as ftc>
    <dc:coverage>${ftc}</dc:coverage>
    </#list>

    <@printIfHasContent dataset.geographicCoverageDescription!; gcd><dc:coverage>${gcd}</dc:coverage></@printIfHasContent>

    <#list dataset.geographicCoverages![] as gc>
    <@printIfHasContent gc.description!; desc><dc:coverage>${desc}</dc:coverage></@printIfHasContent>
        <#if gc.boundingBox?has_content>
            <#assign bbox = gc.boundingBox>
    <dc:coverage>${bbox.minLatitude},${bbox.minLongitude} / ${bbox.maxLatitude},${bbox.maxLongitude} (min, max Latitude / min, max Longitude)</dc:coverage>
        </#if>
    </#list>
    <dc:format>${dc.format}</dc:format>
    <dc:source>Global Biodiversity Information Facility (GBIF) http://www.gbif.org</dc:source>
</oai_dc:dc>
</#escape>