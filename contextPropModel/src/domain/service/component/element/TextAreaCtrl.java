package domain.service.component.element;

import java.util.Collection;

import domain.service.component.element.html.TextArea;


public class TextAreaCtrl extends AbstractCtrl {

	private TextArea input_;

	@Override
	protected void initContent() {
		this.input_ = new TextArea();
		this.input_.setName(this.fieldView.getQualifiedContextName());
		this.input_.setId(this.fieldView.getQualifiedContextName());
		this.input_.setClassId(ICtrl.TEXT_CLASS_ID);
		final boolean disabled = !this.fieldView.isEditable() && !this.fieldView.isHidden()
				&& (this.fieldView.isUserDefined() || !this.fieldView.getEntityField().getAbstractField().isBlob());
		this.input_.setDisabled(this.fieldView.isDisabled() || !this.fieldView.isEditable() ? true : disabled);
		if (this.fieldView.getStyleCss() != null && !"".equals(this.fieldView.getStyleCss()) && !this.fieldView.isEditable()) {// aplico el estilo si es de solo lectura
			this.input_.setStyle(this.fieldView.getStyleCss());
		}		
	}

	@Override
	public String getInnerHtml(final String title, final Collection<String> values_) {
		final String valueOfText = values_.isEmpty() ? "" : values_.iterator().next();
		int[] retornoContarLineas = TextAreaCtrl.contarSaltosLinea(valueOfText);
		int rows_= 4, cols_ = 200, saltosLinea = retornoContarLineas[0] - (retornoContarLineas[0]/2);
		int lengthOftextArea = valueOfText.length();
		
		if (lengthOftextArea < 10){
			rows_= 1;
			cols_ = 30;
		}else if (lengthOftextArea < 20){
			rows_= 2;
			cols_ = 40;
		}else if (lengthOftextArea < 30){
			rows_= 2;
			cols_ = 55;
		}else if (lengthOftextArea >= 30 && lengthOftextArea < 50){
			rows_= 2;
			cols_ = 80;
		}else if (lengthOftextArea >= 50 && lengthOftextArea < 100){
			rows_= 3;
			cols_ = 95;
		}else if (lengthOftextArea >= 100 && lengthOftextArea < 500){
			rows_= 4 + saltosLinea;
			cols_ = 50;
		}else if (lengthOftextArea >= 500 && lengthOftextArea < 1000){
			rows_= 4 + saltosLinea;
			cols_ = 100;
		}else if (lengthOftextArea >= 1000 && lengthOftextArea < 2250){
			rows_= 5 + saltosLinea;
			cols_ = 150;
		}else if (lengthOftextArea >= 2250){
			rows_= 5 + saltosLinea;
		}
		this.input_.setRows(rows_);
		this.input_.setCols(cols_);
		return this.input_.toHTML(values_);
	}
	
	private static final int[] contarSaltosLinea(String textA){
		final int totL = textA.length();
		//int lineaMasLarga = 0, lineaMasLargaActual = 0;
		int contadorSaltos = 0;
		for (int i=0;i<totL;i++){
		  if (textA.charAt(i) == '\n' || textA.charAt(i) == '\r'){
			  contadorSaltos++;
			  // if (lineaMasLargaActual > lineaMasLarga){
			  //	  lineaMasLarga = lineaMasLargaActual;
			  // }
			  //lineaMasLargaActual = 0;
		  }//else{
		  // lineaMasLargaActual++;
		  //}
		}//for
		int[] retorno = new int[1];
		retorno [0] = contadorSaltos;
		//retorno [1] = lineaMasLarga;
		
		return retorno;
	}
	
}
