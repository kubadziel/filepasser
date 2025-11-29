import { test, expect } from "@playwright/test";

const uploadEndpoint =
  process.env.UPLOAD_ENDPOINT ?? "http://localhost:8081/api/upload";
const USE_REAL_BACKEND = process.env.E2E_REAL_BACKEND === "1";
const TEST_EMAIL = process.env.E2E_USER_EMAIL ?? "test@filepasser.local";
const TEST_PASSWORD = process.env.E2E_USER_PASSWORD ?? "Password123!";

const testFilename = "1234567_sample.xml";

test.describe("Upload flow", () => {
  test("displays upload result on success", async ({ page }) => {
    if (!USE_REAL_BACKEND) {
      await page.route(uploadEndpoint, async route => {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            status: "SENT_TO_ROUTER",
            contractId: "1234567",
          }),
        });
      });
    }

    await page.goto("/upload");
    await page.waitForURL("**/realms/**/protocol/openid-connect/auth*", { waitUntil: "domcontentloaded" });
    await page.fill("input#username", TEST_EMAIL);
    await page.fill("input#password", TEST_PASSWORD);
    await page.click("input#kc-login");
    await page.waitForURL("**/upload");
    await expect(page.getByText("Upload XML File")).toBeVisible();

    await page.setInputFiles('input[type="file"]', {
      name: testFilename,
      mimeType: "text/xml",
      buffer: Buffer.from("<pain></pain>"),
    });

    const uploadResponse = await page.waitForResponse(response =>
      response.request().method() === "POST" &&
      response.url().includes("/api/upload")
    );
    expect(uploadResponse.ok()).toBeTruthy();
    const payload = await uploadResponse.json();

    const resultBox = page.getByTestId("upload-result");
    await expect(resultBox).toBeVisible();
    if (payload.contractId) {
      await expect(resultBox).toContainText(payload.contractId);
    }

    if (!USE_REAL_BACKEND) {
      await expect(resultBox).toContainText('"status": "SENT_TO_ROUTER"');
      await expect(resultBox).toContainText('"contractId": "1234567"');
    } else {
      await expect(resultBox).not.toContainText("Upload failed");
    }
  });

  const testOrSkip = USE_REAL_BACKEND ? test.skip : test;

  testOrSkip("shows error message when backend call fails", async ({ page }) => {
    await page.route(uploadEndpoint, async route => {
      await route.fulfill({
        status: 500,
        contentType: "application/json",
        body: JSON.stringify({ message: "boom" }),
      });
    });

    await page.goto("/upload");
    await page.waitForURL("**/realms/**/protocol/openid-connect/auth*", { waitUntil: "domcontentloaded" });
    await page.fill("input#username", TEST_EMAIL);
    await page.fill("input#password", TEST_PASSWORD);
    await page.click("input#kc-login");
    await page.waitForURL("**/upload");

    await page.setInputFiles('input[type="file"]', {
      name: testFilename,
      mimeType: "text/xml",
      buffer: Buffer.from("<pain></pain>"),
    });

    await expect(
      page.getByText("Upload failed", { exact: false })
    ).toBeVisible();
  });
});
