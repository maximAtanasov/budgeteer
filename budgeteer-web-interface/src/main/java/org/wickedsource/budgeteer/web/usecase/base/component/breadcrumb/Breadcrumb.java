package org.wickedsource.budgeteer.web.usecase.base.component.breadcrumb;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.web.usecase.base.BasePage;

import java.io.Serializable;

public class Breadcrumb implements Serializable {

    private Class<? extends BasePage> targetPage;

    private PageParameters parameters;

    private IModel<String> titleModel;

    public Breadcrumb(Class<? extends BasePage> targetPage, String title) {
        this.titleModel = Model.of(title);
        this.targetPage = targetPage;
    }

    public Breadcrumb(Class<? extends BasePage> targetPage, PageParameters parameters, String title) {
        this.titleModel = Model.of(title);
        this.parameters = parameters;
        this.targetPage = targetPage;
    }

    public Breadcrumb(Class<? extends BasePage> targetPage, PageParameters parameters, IModel<String> titleModel) {
        this.parameters = parameters;
        this.targetPage = targetPage;
        this.titleModel = titleModel;
    }

    public Class<?> getTargetPage() {
        return targetPage;
    }

    public IModel<String> getTitleModel() {
        return this.titleModel;
    }

    public PageParameters getParameters() {
        return parameters;
    }
}