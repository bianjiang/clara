<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output omit-xml-declaration="yes" indent="yes"/>

    <xsl:template match="protocol">
      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
      <div id="{concat('protocol_',@id)}" class="protocol-summary-wrapper">
      	<div class="protocol-summary-header">
      		<h1 class="protocol-summary-basic-details-overview-title"><xsl:value-of select="title" /><xsl:value-of select="title" /></h1>
      	</div>
      	<div class="protocol-summary-basic-details">
      		<div class="protocol-summary-basic-details-overview">
      			<h2 class="protocol-summary-basic-details-overview-study-type"><xsl:value-of select="study-type" /></h2>
      			<div class="protocol-summary-basic-details-overview-study-laysummary"><xsl:value-of select="lay-summary" /></div>
      		</div>
      	</div>
      </div>
    </xsl:template>
</xsl:stylesheet>