package com.gitplex.server.web;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Application;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.pegdown.Parser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.google.common.collect.Lists;
import com.gitplex.commons.git.exception.GitException;
import com.gitplex.commons.jetty.ServletConfigurator;
import com.gitplex.calla.loader.AbstractPluginModule;
import com.gitplex.commons.markdown.extensionpoint.HtmlTransformer;
import com.gitplex.commons.markdown.extensionpoint.MarkdownExtension;
import com.gitplex.commons.wicket.AbstractWicketConfig;
import com.gitplex.commons.wicket.ResourcePackScopeContribution;
import com.gitplex.commons.wicket.editable.EditSupport;
import com.gitplex.commons.wicket.websocket.DefaultWebSocketManager;
import com.gitplex.commons.wicket.websocket.WebSocketManager;
import com.gitplex.server.core.manager.UrlManager;
import com.gitplex.server.core.util.validation.AccountNameReservation;
import com.gitplex.server.core.util.validation.DepotNameReservation;
import com.gitplex.server.core.util.validation.TeamNameReservation;
import com.gitplex.server.web.avatar.AvatarManager;
import com.gitplex.server.web.avatar.DefaultAvatarManager;
import com.gitplex.server.web.component.comment.MentionTransformer;
import com.gitplex.server.web.component.comment.PullRequestTransformer;
import com.gitplex.server.web.component.depotfile.blobview.BlobRenderer;
import com.gitplex.server.web.component.diff.DiffRenderer;
import com.gitplex.server.web.editable.EditSupportLocator;
import com.gitplex.server.web.websocket.CodeCommentChangeBroadcaster;
import com.gitplex.server.web.websocket.CommitIndexedBroadcaster;
import com.gitplex.server.web.websocket.PullRequestChangeBroadcaster;
import com.gitplex.server.web.websocket.TaskChangeBroadcaster;

import jersey.repackaged.com.google.common.collect.Sets;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class WebModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(AbstractWicketConfig.class).to(WicketConfig.class);		
		bind(WebApplication.class).to(WicketConfig.class);
		bind(Application.class).to(WicketConfig.class);
		bind(AvatarManager.class).to(DefaultAvatarManager.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);
		
		contribute(ServletConfigurator.class, WebServletConfigurator.class);
		contribute(AccountNameReservation.class, WebAccountNameReservation.class);
		contribute(DepotNameReservation.class, WebDepotNameReservation.class);
		contribute(TeamNameReservation.class, WebTeamNameReservation.class);
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);
		
		bind(CommitIndexedBroadcaster.class);
		
		contributeFromPackage(DiffRenderer.class, DiffRenderer.class);
		contributeFromPackage(BlobRenderer.class, BlobRenderer.class);
		
		contribute(MarkdownExtension.class, new MarkdownExtension() {
			
			@Override
			public Collection<Class<? extends Parser>> getInlineParsers() {
				return null;
			}
			
			@Override
			public Collection<ToHtmlSerializerPlugin> getHtmlSerializers() {
				return null;
			}
			
			@Override
			public Collection<Class<? extends Parser>> getBlockParsers() {
				return null;
			}

			@Override
			public Collection<HtmlTransformer> getHtmlTransformers() {
				return Lists.newArrayList(new MentionTransformer(), new PullRequestTransformer());
			}
			
		});
		contribute(ResourcePackScopeContribution.class, new ResourcePackScopeContribution() {
			
			@Override
			public Collection<Class<?>> getResourcePackScopes() {
				return Lists.newArrayList(WebModule.class);
			}
			
		});
		contribute(ExpectedExceptionContribution.class, new ExpectedExceptionContribution() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Collection<Class<? extends Exception>> getExpectedExceptionClasses() {
				return Sets.newHashSet(ConstraintViolationException.class, EntityNotFoundException.class, 
						ObjectNotFoundException.class, StaleStateException.class, UnauthorizedException.class, 
						GitException.class, PageExpiredException.class, StalePageException.class);
			}
			
		});

		bind(UrlManager.class).to(WicketUrlManager.class);
		bind(CodeCommentChangeBroadcaster.class);
		bind(PullRequestChangeBroadcaster.class);
		bind(TaskChangeBroadcaster.class);
	}
	
}