(function () {
    "use strict";

    const form = document.getElementById("profileForm");
    if (!form) {
        return;
    }

    const autoSaveUrl = form.dataset.autoSaveUrl;
    const saveStatus = document.getElementById("profileSaveStatus");
    const autoSaveFields = Array.from(
        form.querySelectorAll("[data-auto-save-field]")
    );
    const fieldLabels = {
        BIRTH_DATE: "생년월일",
        REGION: "거주지역",
        HOUSEHOLD_SIZE: "가구원 수",
        MONTHLY_EARNED_INCOME: "월 근로·사업소득",
        EMPLOYMENT_STATUS: "취업 상태",
        EDUCATION_STATUS: "학적 상태",
        HOUSING_OWNERSHIP_STATUS: "주택 소유 상태"
    };

    let debounceTimer = null;
    let activeController = null;
    let latestRequestSequence = 0;
    let latestChangeSequence = 0;
    let pendingPatch = {};

    function valueFor(input) {
        if (input.value === "") {
            return null;
        }
        if (input.type === "number") {
            return Number(input.value);
        }
        return input.value;
    }

    function setSaveStatus(state, message) {
        saveStatus.dataset.state = state;
        saveStatus.textContent = message;
    }

    function clearFieldError(fieldName) {
        const input = form.querySelector(
            `[data-auto-save-field="${fieldName}"]`
        );
        const field = input ? input.closest(".form-field") : null;
        if (!field) {
            return;
        }
        field.classList.remove("form-field--error");
        const error = field.querySelector(".field-error");
        if (error) {
            error.remove();
        }
    }

    function showFieldError(fieldName, message) {
        const input = fieldName
            ? form.querySelector(`[data-auto-save-field="${fieldName}"]`)
            : null;
        const field = input ? input.closest(".form-field") : null;
        if (!field) {
            setSaveStatus("error", message);
            return;
        }

        clearFieldError(fieldName);
        field.classList.add("form-field--error");
        const error = document.createElement("span");
        error.className = "field-error";
        error.textContent = message;
        error.setAttribute("role", "alert");
        field.appendChild(error);
    }

    function queuePatch(fieldName, value, immediate) {
        clearFieldError(fieldName);
        pendingPatch[fieldName] = value;
        latestChangeSequence++;
        window.clearTimeout(debounceTimer);

        if (immediate) {
            savePendingPatch();
            return;
        }

        setSaveStatus("waiting", "입력 내용을 확인하고 있어요...");
        debounceTimer = window.setTimeout(savePendingPatch, 550);
    }

    async function savePendingPatch() {
        window.clearTimeout(debounceTimer);
        const patch = pendingPatch;
        pendingPatch = {};

        if (Object.keys(patch).length === 0) {
            return;
        }

        if (activeController) {
            activeController.abort();
        }
        activeController = new AbortController();
        const requestSequence = ++latestRequestSequence;
        const changeSequence = latestChangeSequence;
        setSaveStatus("saving", "저장 중...");

        try {
            const response = await fetch(autoSaveUrl, {
                method: "PATCH",
                credentials: "same-origin",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(patch),
                signal: activeController.signal
            });

            if (response.status === 401) {
                window.location.assign("/login");
                return;
            }

            const body = await response.json().catch(function () {
                return null;
            });

            if (requestSequence !== latestRequestSequence
                    || changeSequence !== latestChangeSequence) {
                return;
            }

            if (!response.ok) {
                const message = body && body.message
                    ? body.message
                    : "저장하지 못했어요. 입력값을 확인해주세요.";
                showFieldError(body ? body.field : null, message);
                setSaveStatus("error", "저장에 실패했어요");
                return;
            }

            updateEligibilitySummary(body);
            setSaveStatus("saved", "저장됨");
        } catch (error) {
            if (error.name === "AbortError") {
                return;
            }
            if (requestSequence === latestRequestSequence
                    && changeSequence === latestChangeSequence) {
                setSaveStatus("error", "저장하지 못했어요. 다시 시도해주세요.");
            }
        }
    }

    function updateEligibilitySummary(response) {
        if (!response || !response.summary) {
            return;
        }

        document.getElementById("eligibleCount").textContent =
            `${response.summary.eligibleCount}개`;
        document.getElementById("needMoreInfoCount").textContent =
            `${response.summary.needMoreInfoCount}개`;
        document.getElementById("ineligibleCount").textContent =
            `${response.summary.ineligibleCount}개`;
        document.getElementById("eligibleBenefitsLink").textContent =
            `신청 가능 정책 ${response.summary.eligibleCount}개 보기`;

        renderMissingFieldGuides(response.missingFieldSummaries || []);
    }

    function recommendationText(summary) {
        const label = fieldLabels[summary.field] || "추가";
        if (summary.singleMissingPolicyCount > 0) {
            return `${label} 정보를 입력하면 `
                + `${summary.singleMissingPolicyCount}개 정책을 더 확인할 수 있어요.`;
        }
        return `${label} 정보가 필요한 정책이 `
            + `${summary.affectedPolicyCount}개 있어요.`;
    }

    function prioritizedSummaries(summaries) {
        return summaries.slice().sort(function (left, right) {
            if (right.singleMissingPolicyCount !== left.singleMissingPolicyCount) {
                return right.singleMissingPolicyCount - left.singleMissingPolicyCount;
            }
            return right.affectedPolicyCount - left.affectedPolicyCount;
        }).slice(0, 2);
    }

    function renderMissingFieldGuides(summaries) {
        const guides = document.getElementById("missingFieldGuides");
        const prioritized = prioritizedSummaries(summaries);
        guides.replaceChildren();

        if (prioritized.length > 0) {
            const title = document.createElement("p");
            title.className = "missing-field-guides__title";
            title.textContent = "정보를 더 입력하면 확인 가능한 정책이 늘어날 수 있어요.";
            guides.appendChild(title);

            const list = document.createElement("ul");
            prioritized.forEach(function (summary) {
                const item = document.createElement("li");
                item.dataset.field = summary.field;
                item.dataset.affectedCount = summary.affectedPolicyCount;
                item.dataset.singleCount = summary.singleMissingPolicyCount;
                item.textContent = recommendationText(summary);
                list.appendChild(item);
            });
            guides.appendChild(list);
        }

        applyFieldRecommendations(prioritized);
    }

    function applyFieldRecommendations(summaries) {
        form.querySelectorAll("[data-profile-field]").forEach(function (field) {
            field.classList.remove("form-field--recommended");
            const previous = field.querySelector(".field-recommendation");
            if (previous) {
                previous.remove();
            }
        });

        summaries.forEach(function (summary) {
            const field = form.querySelector(
                `[data-profile-field="${summary.field}"]`
            );
            if (!field) {
                return;
            }
            field.classList.add("form-field--recommended");
            const hint = document.createElement("span");
            hint.className = "field-recommendation";
            hint.textContent = recommendationText(summary);
            field.appendChild(hint);
        });
    }

    function readInitialMissingSummaries() {
        return Array.from(
            document.querySelectorAll("#missingFieldGuides li[data-field]")
        ).map(function (item) {
            return {
                field: item.dataset.field,
                affectedPolicyCount: Number(item.dataset.affectedCount),
                singleMissingPolicyCount: Number(item.dataset.singleCount)
            };
        });
    }

    autoSaveFields.forEach(function (input) {
        const eventName = input.tagName === "SELECT" ? "change" : "input";
        input.addEventListener(eventName, function () {
            queuePatch(
                input.dataset.autoSaveField,
                valueFor(input),
                input.tagName === "SELECT"
            );
        });
    });

    window.searchAddress = function () {
        if (!window.daum || !window.daum.Postcode) {
            setSaveStatus("error", "주소 검색을 불러오지 못했어요. 다시 시도해주세요.");
            return;
        }

        new window.daum.Postcode({
            oncomplete: function (data) {
                const address = data.address || null;
                const regionCode = [data.sido, data.sigungu]
                    .filter(Boolean)
                    .join(" ") || null;
                document.getElementById("address").value = address || "";
                document.getElementById("regionCode").value = regionCode || "";
                pendingPatch.address = address;
                pendingPatch.regionCode = regionCode;
                latestChangeSequence++;
                clearFieldError("regionCode");
                savePendingPatch();
            }
        }).open();
    };

    applyFieldRecommendations(prioritizedSummaries(readInitialMissingSummaries()));
})();
