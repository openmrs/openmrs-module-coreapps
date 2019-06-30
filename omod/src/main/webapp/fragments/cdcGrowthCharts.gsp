<ul class="navList">
	<li>
		<a id="WTAGEINF" class="growthChartCDCLink" href="#">${ ui.message("coreapps.WTAGEINF") }</a>
	</li>
	<li>
		<a id="LENAGEINF" class="growthChartCDCLink" href="#">${ ui.message("coreapps.LENAGEINF") }</a>
	</li>
	<li>
		<a id="WTLENINF" class="growthChartCDCLink" href="#">${ ui.message("coreapps.WTLENINF") }</a>
	</li>
	<li>
		<a id="HCAGEINF" class="growthChartCDCLink" href="#">${ ui.message("coreapps.HCAGEINF") }</a>
	</li>
	<%if (patientPropts.age.years >= 2) {%>
		<li>
			<a id="WTSTAT" class="growthChartCDCLink" href="#">${ ui.message("coreapps.WTSTAT") }</a>
		</li>
		<li>
			<a id="WTAGE" class="growthChartCDCLink" href="#">${ ui.message("coreapps.WTAGE") }</a>
		</li>
		<li>
			<a id="STATAGE" class="growthChartCDCLink" href="#">${ ui.message("coreapps.STATAGE") }</a>
		</li>
		<li>
			<a id="BMIAGE" class="growthChartCDCLink" href="#">${ ui.message("coreapps.BMIAGE") }</a>
		</li>
	<%}%>
</ul>
<br />
<div id="cdc_growth_charts-container">
	<canvas id="cdc_growth_charts"></canvas>
</div>