package com.erfangc.sac.backend.tests;

import com.erfangc.sac.interfaces.*;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class StatefulTestBase {

    protected SimpleAccessControl sac;

    @Test
    public void test() {
        // we simulate a show biz / film production system

        /*
        Groups
         - writers to write the scripts
         - directors to make/edit the films
         - execs to manage the business

         all execs are also directors
         */
        ImmutableGroup writers = ImmutableGroup
                .builder()
                .id("writers")
                .name("Writers")
                .description("The writing team for writing scripts")
                .build();

        ImmutableGroup directors = ImmutableGroup
                .builder()
                .id("directors")
                .name("Directors")
                .description("Directors of new films")
                .build();

        ImmutableGroup execs = ImmutableGroup
                .builder()
                .id("execs")
                .name("Executives")
                .description("Administrators & Executives")
                .build();

        ImmutableGroup allTeamMembers = ImmutableGroup
                .builder()
                .id("all team members")
                .name("All team members")
                .build();

        /*
        Identity policies
         */
        ImmutableIdentityPolicy viewFilms = ImmutableIdentityPolicy
                .builder()
                .id("view films")
                .name("View films")
                .resource("/films/*")
                .actions(singletonList("view"))
                .build();

        ImmutableIdentityPolicy releaseFilms = ImmutableIdentityPolicy
                .builder()
                .id("release films")
                .name("Release films")
                .resource("/films/*")
                .actions(singletonList("release"))
                .build();

        ImmutableIdentityPolicy createAndEditFilms = ImmutableIdentityPolicy
                .builder()
                .id("create and edit films")
                .name("Create and edit films")
                .resource("/films/*")
                .actions(asList("create", "edit", "delete"))
                .build();

        ImmutableIdentityPolicy manageScripts = ImmutableIdentityPolicy
                .builder()
                .id("produce scripts")
                .name("Produce scripts")
                .actions(singletonList("*"))
                .resource("/scripts/*")
                .build();

        sac.createGroup(allTeamMembers);
        sac.createGroup(execs);
        sac.createGroup(directors);
        sac.createGroup(writers);

        sac.createPolicy(releaseFilms);
        sac.createPolicy(viewFilms);
        sac.createPolicy(createAndEditFilms);
        sac.createPolicy(manageScripts);

        /*
        Assign policies
        - directors can produce and create files
        - executives can release the films && inherit directors' privileges
        - writers can write scripts
         */
        sac.assignPolicy(releaseFilms.id(), execs.id());
        sac.assignPolicy(createAndEditFilms.id(), directors.id());
        sac.assignPolicy(manageScripts.id(), writers.id());
        sac.assignPolicy(viewFilms.id(), allTeamMembers.id());

        sac.assignPrincipalToGroup(allTeamMembers.id(), execs.id(), true);
        sac.assignPrincipalToGroup(allTeamMembers.id(), directors.id(), true);
        sac.assignPrincipalToGroup(directors.id(), execs.id(), true);
        sac.assignPrincipalToGroup(allTeamMembers.id(), writers.id(), true);

        String johnTheExecutive = "john";
        String jackTheDirector = "jack";
        String abelTheDirector = "abel";
        String jillTheWriter = "jill";

        sac.assignPrincipalToGroup(execs.id(), johnTheExecutive);
        sac.assignPrincipalToGroup(directors.id(), jackTheDirector);
        sac.assignPrincipalToGroup(directors.id(), abelTheDirector);
        sac.assignPrincipalToGroup(writers.id(), jillTheWriter);

        /*
        Jack tries to release a film should not be allowed, since he is only a director and not an executive
         */
        final ImmutableAuthorizationRequest jackToReleaseAFilm = ImmutableAuthorizationRequest
                .builder()
                .id("1")
                .resource("/films/gone with the wind")
                .action("release")
                .principal(jackTheDirector)
                .build();
        assertEquals(AuthorizationStatus.Denied, sac.authorize(jackToReleaseAFilm).status());

        /*
        John tries to release a film should be allowed, since he is an executive
         */
        final ImmutableAuthorizationRequest johnToReleaseAFilm = ImmutableAuthorizationRequest
                .builder()
                .id("1")
                .resource("/films/gone with the wind")
                .action("release")
                .principal(johnTheExecutive)
                .build();
        assertEquals(AuthorizationStatus.Permitted, sac.authorize(johnToReleaseAFilm).status());

        /*
        Jill can tear a script apart but even John can't
         */
        final ImmutableAuthorizationRequest jillToDeleteScript = ImmutableAuthorizationRequest
                .builder()
                .id("2")
                .principal(jillTheWriter)
                .resource("/scripts/untitled1")
                .action("delete")
                .build();
        assertEquals(AuthorizationStatus.Permitted, sac.authorize(jillToDeleteScript).status());
        final ImmutableAuthorizationRequest johnToDeleteScript = ImmutableAuthorizationRequest
                .builder()
                .id("2")
                .principal(johnTheExecutive)
                .resource("/scripts/untitled1")
                .action("delete")
                .build();
        assertEquals(AuthorizationStatus.Denied, sac.authorize(johnToDeleteScript).status());

        /*
        But Jill cannot edit films as she is not a director
         */
        final ImmutableAuthorizationRequest jillToEditFilm = ImmutableAuthorizationRequest
                .builder()
                .id("3")
                .resource("/films/gone with the wind")
                .principal(jillTheWriter)
                .action("edit")
                .build();
        assertEquals(AuthorizationStatus.Denied, sac.authorize(jillToEditFilm).status());

        /*
        Once we promote Jill to also be a director, she can now edit films
         */
        sac.assignPrincipalToGroup(directors.id(), jillTheWriter);
        assertEquals(AuthorizationStatus.Permitted, sac.authorize(jillToEditFilm).status());

        /*
        Let's have the characters work on an special project to test resource based policy
         */
        sac.grantActions("/special idea/oscar winning idea", jillTheWriter, singleton("write"));

        final ImmutableAuthorizationRequest requestToWriteOscarIdea = ImmutableAuthorizationRequest
                .builder()
                .id("4")
                .principal("")
                .resource("/special idea/oscar winning idea")
                .action("write")
                .build();

        assertEquals(AuthorizationStatus.Permitted, sac.authorize(requestToWriteOscarIdea.withPrincipal(jillTheWriter)).status());
        assertEquals(AuthorizationStatus.Denied, sac.authorize(requestToWriteOscarIdea.withPrincipal(johnTheExecutive)).status());
        assertEquals(AuthorizationStatus.Denied, sac.authorize(requestToWriteOscarIdea.withPrincipal(abelTheDirector)).status());
        assertEquals(AuthorizationStatus.Denied, sac.authorize(requestToWriteOscarIdea.withPrincipal(jackTheDirector)).status());

        sac.revokeActions("/special idea/oscar winning idea", jillTheWriter, singleton("write"));
        assertEquals(AuthorizationStatus.Denied, sac.authorize(requestToWriteOscarIdea.withPrincipal(jillTheWriter)).status());

    }
}
