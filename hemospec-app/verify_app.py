from playwright.sync_api import sync_playwright, expect
import time

def verify_app_flow():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        # Capture console logs
        page.on("console", lambda msg: print(f"BROWSER CONSOLE: {msg.text}"))

        # 1. Start App -> Splash Screen
        print("Navigating to app...")
        page.goto("http://localhost:5173")

        # Wait for splash screen to start
        print("Waiting for splash...")
        page.wait_for_timeout(1000)

        # Wait for verification sequence to finish (approx 5-6s)
        print("Waiting for verification sequence...")
        page.wait_for_timeout(7000)

        # 2. Pre-Login (Landing)
        print("Verifying landing page...")
        expect(page.get_by_role("button", name="Login")).to_be_visible()

        # Click Login
        page.get_by_role("button", name="Login").click()

        # 3. Login Form
        print("Verifying login form...")

        email_native = page.locator('input[type="email"]').first
        email_native.fill("daniel.sousa@labinlight.com")
        email_native.blur()

        password_native = page.locator('input[type="password"]').first
        password_native.fill("secret")
        password_native.blur()

        # Click Login button
        page.get_by_role("button", name="Login").click()

        # 4. Home Screen
        print("Verifying home screen...")

        # Wait and check for home screen
        page.wait_for_timeout(3000)
        expect(page.get_by_text("New test")).to_be_visible()
        print("Home screen verified.")

        # 5. Device Connection
        print("Starting New Test (Device Connection)...")
        page.get_by_text("New test").click()

        # Wait for connection modal and flow
        print("Waiting for device connection flow...")
        # Should see "Verifying Bluetooth" -> "Scanning" -> "Device Ready"
        # Since logic mocks it, it should take about 4-5 seconds total to reach "Ready"

        # Wait for "Device Ready" text
        try:
            expect(page.get_by_text("Device Ready", exact=False)).to_be_visible(timeout=10000)
            print("Device Connected and Ready.")
        except Exception as e:
            # Maybe it asked to select a device?
            if page.get_by_text("Select Device").is_visible():
                print("Selecting device...")
                page.get_by_text("Hemospec Device A1").click()
                expect(page.get_by_text("Device Ready", exact=False)).to_be_visible(timeout=10000)
            else:
                raise e

        # 6. Patient Form
        print("Verifying Patient Form...")
        expect(page.get_by_text("Patient Information")).to_be_visible()

        # Fill form
        page.locator('input[placeholder="12345"]').fill("TEST-101")
        page.locator('input[placeholder="12345"]').blur()

        page.locator('input[placeholder="30"]').fill("45")
        page.locator('input[placeholder="30"]').blur()

        page.get_by_text("Continue").click()

        # 7. Analysis
        print("Verifying Analysis...")
        expect(page.get_by_text("Scanning...")).to_be_visible()

        # Wait for analysis complete
        print("Waiting for analysis results...")
        expect(page.get_by_text("Analysis Complete", exact=False)).to_be_visible(timeout=15000)

        print("Analysis complete. Checking results...")
        expect(page.get_by_text("TEST-101")).to_be_visible()

        page.get_by_text("Done").click()

        # Back to Home
        expect(page.get_by_text("New test")).to_be_visible()

        # 8. History
        print("Verifying History...")
        page.get_by_text("History").click()
        expect(page.get_by_text("History").first).to_be_visible()

        # Wait for history load
        page.wait_for_timeout(2000)
        # Check if items are listed (might be empty if API is fresh, but we did one test?
        # Actually API logic for history might not auto-save unless backend does it.
        # The 'analyze' endpoint returns result but does it save?
        # AnalysisService.analyze usually saves.
        # So we should see at least one item.

        # Refresh
        page.get_by_role("button").filter(has_text=None).last.click() # Refresh button icon

        print("Verification flow complete!")
        browser.close()

if __name__ == "__main__":
    verify_app_flow()
