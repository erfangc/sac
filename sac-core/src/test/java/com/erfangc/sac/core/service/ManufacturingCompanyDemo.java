package com.erfangc.sac.core.service;

import com.erfangc.sac.interfaces.ImmutableAuthorizationRequest;
import com.erfangc.sac.interfaces.ImmutableGroup;
import com.erfangc.sac.interfaces.ImmutableIdentityPolicy;
import com.erfangc.sac.interfaces.SimpleAccessControl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * This is a text based demo illustrating how Access Control can be applied
 * to a hypothetical manufacturing company that produce baby toys
 */
public class ManufacturingCompanyDemo {

    private static final int black = 30;
    private static final int red = 31;
    private static final int green = 32;
    private static final int yellow = 33;
    private static final int blue = 34;
    private static final int magenta = 35;
    private static final int cyan = 36;
    private static final int white = 37;

    private static final ImmutableGroup manufacturingTeam = ImmutableGroup
            .builder()
            .id("manufacturing")
            .name("Manufacturing team")
            .build();
    private static final ImmutableGroup salesTeam = ImmutableGroup
            .builder()
            .id("sales")
            .name("Sales team")
            .build();
    private static final ImmutableGroup executives = ImmutableGroup
            .builder()
            .id("executives")
            .name("Executives team")
            .build();
    private static final SimpleAccessControl sac = InMemorySimpleAccessControl.getInstance();
    private static ObjectWriter objectWriter = new ObjectMapper(new YAMLFactory())
            .findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerWithDefaultPrettyPrinter();
    private static String apiV1 = "auth.mcorp.com/api/v1/";

    /*
    Policies
     */
    private static ImmutableIdentityPolicy operateAssemblyLine = ImmutableIdentityPolicy
            .builder()
            .id("operate-assembly-line")
            .name("Policy to operate the assembly line")
            .actions(asList("start", "pause"))
            .resource("/assembly-lines/*")
            .build();
    private static ImmutableIdentityPolicy manageProducts = ImmutableIdentityPolicy
            .builder()
            .id("sell-products")
            .name("Policy to manage product sales")
            .actions(asList("sell", "restock"))
            .resource("/products/*")
            .build();
    private static ImmutableIdentityPolicy setSalaries = ImmutableIdentityPolicy
            .builder()
            .id("set-salaries")
            .name("Policy to set salaries of other employees")
            .actions(asList("increase", "decrease"))
            .resource("/employees/*")
            .build();

    private static void setConsoleColor(int color) {
        System.out.println((char) 27 + "[" + color + "m");
    }

    public static void main(String[] args) {
        realPromptEnterKey();
        showIntro();
        pause();
        createGroups();
        pause();
        createPolicies();
        pause();
        hireEmployees();
        runTests();
    }

    private static void runTests() {
        final ImmutableAuthorizationRequest toddToSellPacifier = ImmutableAuthorizationRequest
                .builder()
                .id("1")
                .action("sell")
                .principal("todd")
                .resource("/products/pacifier")
                .build();
        final ImmutableAuthorizationRequest sallyToSellPacifier = toddToSellPacifier.withPrincipal("sally");

        pause();

        printBlue("Todd (the manufacturing guy) shouldn't be able to sell pacifiers, since he is not part of the sales team:");
        printRequest(new Request().setMethod("POST").setUrl(apiV1 + "authorize").setBody(toddToSellPacifier));
        fakePromptEnterKey();
        printRed("Response status: " + sac.authorize(toddToSellPacifier).status());

        pause();

        printBlue("Sally on the other hand should be permitted since she belongs to the sales team:");
        printRequest(new Request().setMethod("POST").setUrl(apiV1 + "authorize").setBody(sallyToSellPacifier));
        fakePromptEnterKey();
        printGreen("Response status: " + sac.authorize(sallyToSellPacifier).status());

        pause();

        printBlue("Let's say we need to raise sales of baby pacifiers, so the company want Todd's help.\nHowever, we don't want Todd, who is from manufacturing, to sell just any item, we only want him to sell baby pacifiers");
        ImmutableIdentityPolicy tempPolicy = ImmutableIdentityPolicy
                .builder()
                .id("temp-policy")
                .resource("/products/pacifier")
                .actions(singletonList("sell"))
                .build();
        printRequest(new Request().setUrl(apiV1 + "policies").setBody(tempPolicy));
        printRequest(new Request().setUrl(apiV1 + "policies/" + tempPolicy.id() + "/_assign?principal=todd"));
        fakePromptEnterKey();
        sac.createPolicy(tempPolicy);
        sac.assignPolicy(tempPolicy.id(), "todd");
        printGreen("Done!");

        pause();

        printBlue("Now Todd can sell pacifiers");
        printRequest(new Request().setMethod("POST").setUrl("authorize").setBody(toddToSellPacifier));
        fakePromptEnterKey();
        printGreen("Response status: " + sac.authorize(toddToSellPacifier).status());

        pause();

        printBlue("But Todd can't sell anything else");
        printRequest(new Request().setMethod("POST").setUrl("authorize").setBody(toddToSellPacifier.withResource("/products/baby-legos")));
        fakePromptEnterKey();
        printRed("Response status: " + sac.authorize(toddToSellPacifier.withResource("/products/baby-legos")).status());

        pause();

        printBlue("Once we are done, we can revoke Todd's permissions by unassigning him from the policy");
        printRequest("POST", apiV1 + "policies/" + tempPolicy.id() + "_unassign?principal=todd");
        fakePromptEnterKey();
        sac.unAssignPolicy(tempPolicy.id(), "todd");
        printGreen("Done!");

        pause();

        printBlue("Now Todd can no longer sell pacifiers");
        printRequest(new Request().setMethod("POST").setUrl("authorize").setBody(toddToSellPacifier));
        fakePromptEnterKey();
        printRed("Response status: " + sac.authorize(toddToSellPacifier).status());
    }

    private static void hireEmployees() {
        printBlue("Let's hire John the executive:");
        printRequest("POST", apiV1 + "groups/" + executives.id() + "/_assign?principal=john");
        fakePromptEnterKey();
        sac.assignPrincipalToGroup(executives.id(), "john");
        printGreen("Hired");
        pause();

        printBlue("Let's hire Todd the production guy:");
        printRequest(new Request().setMethod("POST").setUrl(apiV1 + "groups/" + manufacturingTeam.id() + "/_assign?principal=todd"));
        fakePromptEnterKey();
        sac.assignPrincipalToGroup(manufacturingTeam.id(), "todd");
        printGreen("Hired");
        pause();

        printBlue("Let's hire Sally the sales gal:");
        printRequest("POST", apiV1 + "groups/" + salesTeam.id() + "/_assign?principal=sally");
        fakePromptEnterKey();
        sac.assignPrincipalToGroup(salesTeam.id(), "sally");
        printGreen("Hired");
        pause();
    }

    private static void createPolicies() {
        printBlue("Let's now create a policy to allow people to operate the assembly line:");
        printRequest(new Request().setMethod("POST").setUrl(apiV1 + "policies").setBody(operateAssemblyLine));
        fakePromptEnterKey();
        sac.createPolicy(operateAssemblyLine);
        printGreen("Created!");

        pause();

        printBlue("We need to make sure only manufacturing team members are assigned this policy to operate the assembly line:");
        printRequest("POST", apiV1 + "policies/" + operateAssemblyLine.id() + "/_assign?principal=" + manufacturingTeam.id());
        fakePromptEnterKey();
        sac.assignPolicy(operateAssemblyLine.id(), manufacturingTeam.id());

        pause();

        printBlue("Create a policy to allow people to sell finished products:");
        printRequest(new Request().setMethod("POST").setUrl(apiV1 + "policies").setBody(manageProducts));
        fakePromptEnterKey();
        sac.createPolicy(manageProducts);
        printGreen("Created!");

        pause();

        printBlue("We need to make sure only the sales team can manage product sales and refunds:");
        printRequest(new Request().setMethod("POST").setUrl(apiV1 + "policies/" + manageProducts.id() + "/_assign?principal=" + salesTeam.id()));
        fakePromptEnterKey();
        sac.assignPolicy(manageProducts.id(), salesTeam.id());
        printGreen("Created!");

        printBlue("Along those lines, let's create a policy so only executives can set salaries:");
        printRequest(new Request().setMethod("POST").setUrl(apiV1 + "policies").setBody(setSalaries));
        printRequest("POST", "policies/" + setSalaries.id() + "/_assign?principal=" + executives.id());
        fakePromptEnterKey();
        sac.createPolicy(setSalaries);
        sac.assignPolicy(setSalaries.id(), executives.id());
        printGreen("Created!");
    }

    private static void createGroups() {
        printRequest(new Request().setUrl("auth.mcorp.com/api/v1/groups").setMethod("POST").setBody(manufacturingTeam));
        fakePromptEnterKey();
        sac.createGroup(manufacturingTeam);
        printGreen("Created!");

        pause();

        printRequest(new Request().setUrl(apiV1 + "groups").setMethod("POST").setBody(salesTeam));
        fakePromptEnterKey();
        sac.createGroup(salesTeam);
        printGreen("Created!");

        pause();

        printRequest(new Request().setUrl(apiV1 + "groups").setMethod("POST").setBody(executives));
        fakePromptEnterKey();
        sac.createGroup(executives);
        printGreen("Created!");
    }

    private static void pause() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void showIntro() {
        printBlue("Welcome to MCorp. we are a new business with no customers or employees yet! Our business takes environmentally safe plastics and rubber and turn them into baby toys.\nLet's create 3 teams before we start hiring: manufacturing, executives and sales:");
        pause();
    }

    private static void printRequest(String method, String url) {
        printRequest(new Request().setMethod(method).setUrl(url));
    }

    private static void printRequest(Request request) {
        setConsoleColor(magenta);
        System.out.println("-------------------------- Request to Execute ---------------------------------------------");
        System.out.println("/" + request.method + " " + request.url);
        if (request.body != null) {
            try {
                System.out.println(objectWriter.writeValueAsString(request.body));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        System.out.println("------------------------------------------------------------------------------------------");
        setConsoleColor(black);
    }

    private static void printGreen(String message) {
        setConsoleColor(green);
        System.out.println(message);
        setConsoleColor(black);
    }

    private static void printBlue(String message) {
        setConsoleColor(blue);
        System.out.println(message);
        setConsoleColor(black);
    }

    private static void printRed(String message) {
        setConsoleColor(red);
        System.out.println(message);
        setConsoleColor(black);
    }

    private static void fakePromptEnterKey() {
        System.out.print("Press \"ENTER\" to submit the request...");
        try {
            Thread.sleep(TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void realPromptEnterKey() {
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private static class Request {
        String method;
        String url;
        Object body;

        Request setMethod(String method) {
            this.method = method;
            return this;
        }

        Request setUrl(String url) {
            this.url = url;
            return this;
        }

        Request setBody(Object body) {
            this.body = body;
            return this;
        }
    }

}
