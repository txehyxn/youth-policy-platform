package com.taehyun.youthpolicyplatform;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCategory;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitCategoryRepository;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitConditionRepository;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.user.domain.Role;
import com.taehyun.youthpolicyplatform.user.domain.User;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import com.taehyun.youthpolicyplatform.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserPageRenderingTest {

    private static final String PASSWORD = "render-test-password";

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BenefitCategoryRepository categoryRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @Autowired
    private BenefitConditionRepository conditionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void publicUserPagesRenderWithCommonLayout() throws Exception {
        HttpClient client = newClient();

        for (String path : new String[]{"/", "/login", "/signup", "/benefits", "/calendar"}) {
            HttpResponse<String> response = get(client, path);

            assertThat(response.statusCode()).as(path).isEqualTo(200);
            assertThat(response.body()).as(path)
                    .contains("/css/common.css", "site-header", "site-footer");
        }

        HttpResponse<String> stylesheet = get(client, "/css/common.css");
        assertThat(stylesheet.statusCode()).isEqualTo(200);
        assertThat(stylesheet.body()).contains("--color-primary");
    }

    @Test
    void navigationChangesForUserAndAdmin() throws Exception {
        User user = saveUser("render-user@example.com", Role.USER);
        User admin = saveUser("render-admin@example.com", Role.ADMIN);

        HttpResponse<String> anonymousHome = get(newClient(), "/");
        assertThat(anonymousHome.body())
                .contains("href=\"/login\"")
                .doesNotContain("href=\"/my/profile\"");

        HttpClient userClient = newClient();
        login(userClient, user.getEmail());
        HttpResponse<String> userHome = get(userClient, "/");
        assertThat(userHome.body())
                .contains("href=\"/my/profile\"", "action=\"/logout\"")
                .doesNotContain("href=\"/admin/benefits\"");
        assertThat(get(userClient, "/admin/benefits").statusCode()).isEqualTo(403);

        for (String path : new String[]{"/my/profile", "/my/bookmarks/calendar"}) {
            HttpResponse<String> response = get(userClient, path);
            assertThat(response.statusCode()).as(path).isEqualTo(200);
            assertThat(response.body()).as(path)
                    .contains("/css/common.css", "site-header", "site-footer");
        }

        HttpClient adminClient = newClient();
        login(adminClient, admin.getEmail());
        HttpResponse<String> adminHome = get(adminClient, "/");
        assertThat(adminHome.body()).contains("href=\"/admin/benefits\"");
        assertThat(get(adminClient, "/admin/benefits").statusCode()).isEqualTo(200);
    }

    @Test
    void missingProfilePageRendersAndKeepsOriginalBenefitLink() throws Exception {
        User user = saveUser("no-profile@example.com", Role.USER);
        HttpClient client = newClient();
        login(client, user.getEmail());

        HttpResponse<String> response = get(client, "/eligibility/check?benefitId=91");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("맞춤 정책 판정을 위해 프로필 정보가 필요합니다")
                .contains("/my/profile?returnBenefitId=91")
                .contains("/benefits/91");
    }

    @Test
    void eligibilityResultRendersPolicyActions() throws Exception {
        User user = saveUser("eligible-user@example.com", Role.USER);
        userProfileRepository.save(new UserProfile(
                25,
                "서울특별시 마포구",
                1,
                2_000_000,
                24_000_000,
                80,
                true,
                false,
                false,
                user
        ));

        BenefitCategory category = categoryRepository.save(new BenefitCategory("주거"));
        Benefit benefit = benefitRepository.save(new Benefit(
                "렌더링 테스트 정책",
                "정책 설명",
                "월 20만원 지원",
                "https://example.com/apply",
                category
        ));
        conditionRepository.save(new BenefitCondition(
                "age", ">=", "19", true, benefit
        ));

        HttpClient client = newClient();
        login(client, user.getEmail());

        HttpResponse<String> detailResponse = get(client, "/benefits/" + benefit.getId());
        assertThat(detailResponse.statusCode()).isEqualTo(200);
        assertThat(detailResponse.body())
                .contains("렌더링 테스트 정책", "/css/common.css", "site-header", "site-footer");

        HttpResponse<String> response = get(
                client,
                "/eligibility/check?benefitId=" + benefit.getId()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("렌더링 테스트 정책", "ELIGIBLE", "월 20만원 지원")
                .contains("/benefits/" + benefit.getId())
                .contains("https://example.com/apply")
                .contains("/bookmarks/" + benefit.getId());
    }

    private User saveUser(String email, Role role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        new User(email, passwordEncoder.encode(PASSWORD), role)
                ));
    }

    private HttpClient newClient() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        return HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    private void login(HttpClient client, String email) throws Exception {
        String form = "username=" + encode(email) + "&password=" + encode(PASSWORD);
        HttpRequest request = HttpRequest.newBuilder(uri("/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
