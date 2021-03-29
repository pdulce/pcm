/**
 * jQuery Plugin for creating collapsible fieldset.
 *
 * Copyright (c) 2013 Mirza Busatlic
 */

(function($) {
  
	$.fn.collapsible = function(options) {
		
		var settings = $.extend({
			collapsed: false, 
			animation: true, 
			speed: "medium",
			valuesFilled: options
		}, options);
		
		this.each(function() {
			var $fieldset = $(this);
			var $legend = $fieldset.children("legend");
			var isCollapsed = $fieldset.hasClass("collapsed");
			
			$legend.click(function() {
				collapse($fieldset, settings, !isCollapsed);
				isCollapsed = !isCollapsed;
			});
			var textoDiagrama = $legend.text();
			var consignedCritFechas = false;
			var consignedCritLocaliz = false;
			var consignedCritAplicativo = false;
			var consignedCritSituacion = false;
			var consignedCritDetalles = false;
			var conjuntoCritFechas = ["18","19","22","23","37","20","21"];
			var conjuntoCritLocaliz = ["17","9","12","41"];
			var conjuntoCritAplicativo = ["26","13"];
			var conjuntoCritSituacion = ["7"];
			var conjuntoCritDetalles = ["1","35","2","3","16"];
			var criteriaFilled = settings.valuesFilled;
			
			//troceamos con split, y recorremos cada elemento, si esto en una lista, activamos su flag correspondiente
			if (criteriaFilled != 'null'){
				var listaFilled = criteriaFilled.split(",");
				for (var i=0;i<listaFilled.length;i++){
					var elemento = "".concat(listaFilled[i]);					
					for (var j=0;j<conjuntoCritFechas.length;j++){
						if (conjuntoCritFechas[j].length==elemento.length && conjuntoCritFechas[j].indexOf(elemento) > -1){
							//alert(elemento + " included in legendFieldset CritFechas");
							consignedCritFechas = consignedCritFechas || true;
						}
					}
					for (var j=0;j<conjuntoCritLocaliz.length;j++){
						if (conjuntoCritLocaliz[j].length==elemento.length && conjuntoCritLocaliz[j].indexOf(elemento) > -1){
							//alert(elemento + " included in legendFieldset CritLocaliz");
							consignedCritLocaliz = consignedCritLocaliz || true;
						}
					}
					for (var j=0;j<conjuntoCritAplicativo.length;j++){
						if (conjuntoCritAplicativo[j].length==elemento.length && conjuntoCritAplicativo[j].indexOf(elemento) > -1){
							//alert(elemento + " included in legendFieldset CritAplicativo");
							consignedCritAplicativo = consignedCritAplicativo || true;
						}
					}
					for (var j=0;j<conjuntoCritSituacion.length;j++){
						if (conjuntoCritSituacion[j].length==elemento.length && conjuntoCritSituacion[j].indexOf(elemento) > -1){
							//alert(elemento + " included in legendFieldset CritSituacion");
							consignedCritSituacion = consignedCritSituacion || true;
						}
					}
					for (var j=0;j<conjuntoCritDetalles.length;j++){
						if (conjuntoCritDetalles[j].length==elemento.length && conjuntoCritDetalles[j].indexOf(elemento) > -1){
							//alert(elemento + " included in legendFieldset CritDetalles");
							consignedCritDetalles = consignedCritDetalles || true;
						}
					}
				}
				//alert('consignado criterio');
			}
			
			if ((textoDiagrama.lastIndexOf("Criterios por Fechas") > -1 && !consignedCritFechas)						
							|| (textoDiagrama.lastIndexOf("Por Situacion") > -1 && !consignedCritSituacion)
							|| (textoDiagrama.lastIndexOf("Por Detalle") > -1 && !consignedCritDetalles)
							|| (textoDiagrama.lastIndexOf("Por Localizacion") > -1 && !consignedCritLocaliz)
				){			 	
			 	isCollapsed = true;
			}else{
				isCollapsed = false;
			}
			
			if (textoDiagrama.lastIndexOf("Diagramas Peticiones GEDEON") > -1 ){
				isCollapsed = true;
			}else if (textoDiagrama.lastIndexOf("Diagramas Estudios de Peticiones") > -1 ){
				isCollapsed = false;
			}else if (textoDiagrama.lastIndexOf("Agregados en Periodo Estudio") > -1 ||
					textoDiagrama.lastIndexOf("Promedios Mensuales") > -1 ||
					textoDiagrama.lastIndexOf("Promedios Por Aplicación") > -1){
				isCollapsed = true;
			}
			
			// Perform initial collapse.
			// Don't use animation to close for initial collapse.
			if(isCollapsed) {
				//alert("tratamiento por colapsado");
				collapse($fieldset, {animation: false}, isCollapsed);
			} else {				
				collapse($fieldset, settings, isCollapsed);
			}
			
		});
	};
		
	/**
	 * Collapse/uncollapse the specified fieldset.
	 * @param {object} $fieldset
	 * @param {object} options
	 * @param {boolean} collapse
	 */
	function collapse($fieldset, options, doCollapse) {
		$container = $fieldset.find("div");
		if(doCollapse) {
			if(options.animation) {
				$container.slideUp(options.speed);
			} else {
				$container.hide();
			}
			$fieldset.removeClass("expanded").addClass("collapsed");
		} else {
			if(options.animation) {
				$container.slideDown(options.speed);
			} else {
				$container.show();
			}
			$fieldset.removeClass("collapsed").addClass("expanded");
		}
	};
	
})(jQuery);
