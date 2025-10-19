# Setting Up Real API Integration

This guide shows you how to switch from mock data to real APIs for production use.

---

## 🔑 Required API Keys

You need to sign up for two services:

### 1. OpenAI (for AI Career Advice)
- **URL:** https://platform.openai.com/signup
- **Cost:** Pay-as-you-go (~$0.002 per request)
- **What we use:** GPT-4 for personalized career advice

### 2. RapidAPI - JSearch (for Real Job Data)
- **URL:** https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch
- **Cost:** Free tier: 250 requests/month
- **What we use:** Real-time job market data and skill demands

---

## 📋 Step-by-Step Setup

### Step 1: Get OpenAI API Key

1. Go to https://platform.openai.com/signup
2. Sign up / Log in
3. Navigate to **API Keys** section
4. Click **"Create new secret key"**
5. Copy the key (starts with `sk-...`)
6. **IMPORTANT:** Save it somewhere safe - you can't see it again!

**What it looks like:**
```
sk-proj-1234567890abcdefghijklmnopqrstuvwxyz...
```

---

### Step 2: Get RapidAPI Key

1. Go to https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch
2. Sign up / Log in
3. Click **"Subscribe to Test"**
4. Choose **"Basic" plan** (Free - 250 requests/month)
5. In the **Code Snippets** section, find:
   - `X-RapidAPI-Key: your_key_here`
   - `X-RapidAPI-Host: jsearch.p.rapidapi.com`
6. Copy your API key

**What it looks like:**
```
1234567890abcdefghijklmnopqrstuvwxyz1234567890
```

---

### Step 3: Add Keys to AI Service

**File:** `backend/ai-service/.env`

```bash
# OpenAI Configuration
OPENAI_API_KEY=sk-proj-YOUR_ACTUAL_KEY_HERE

# Service Configuration (don't change these)
SERVICE_PORT=8001
SERVICE_HOST=0.0.0.0
BACKEND_URL=http://localhost:8080
```

**After saving:**
```bash
# Restart AI service
# (Stop with Ctrl+C if running, then:)
cd backend/ai-service
python3 run.py
```

You should see:
```
OpenAI Configured: True  # <-- This should now be True!
```

---

### Step 4: Add Keys to Spring Boot

**File:** `backend/skillsync-api/src/main/resources/application.yml`

Find the `external` section and update:

```yaml
# External API Configuration
external:
  openai:
    api-key: sk-proj-YOUR_ACTUAL_KEY_HERE  # Add your key
    base-url: http://localhost:8001
  rapidapi:
    key: YOUR_RAPIDAPI_KEY_HERE  # Add your key
    host: jsearch.p.rapidapi.com
```

---

### Step 5: Switch to Production Profile

**File:** `backend/skillsync-api/src/main/resources/application.yml`

Change this line:

```yaml
spring:
  profiles:
    active: prod  # Change from 'dev' to 'prod'
```

**After saving:**
```bash
# Restart Spring Boot
# (Stop with Ctrl+C if running, then:)
cd backend/skillsync-api
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```

---

## 🧪 Testing Real APIs

### Test 1: OpenAI AI Advice

```bash
# Check AI service status
curl http://localhost:8001/health

# Should show:
# "openai_configured": true  ✅
```

**Test with real GPT-4:**
```bash
curl -X POST http://localhost:8001/api/ai/advice \
  -H "Content-Type: application/json" \
  -d '{
    "user_profile": {
      "user_id": "test",
      "name": "John",
      "current_role": "Junior Developer",
      "target_role": "Senior Full Stack Developer",
      "current_skills": ["JavaScript", "React"],
      "experience_years": 2
    },
    "skill_gap": {
      "skill_gap_percentage": 60.0,
      "alignment_score": 40.0,
      "missing_skills": ["Node.js", "TypeScript", "Docker"],
      "matching_skills_count": 4,
      "missing_skills_count": 6
    },
    "question": "What's the fastest way to become a Senior Full Stack Developer?"
  }'
```

**You should get:**
- Detailed, personalized advice from GPT-4
- More sophisticated reasoning
- Specific action steps tailored to your profile

---

### Test 2: RapidAPI Job Data

```bash
# Sync real job data for Backend Engineer
curl -X POST http://localhost:8080/api/jobs/sync \
  -H "Content-Type: application/json" \
  -d '{
    "role": "Backend Engineer",
    "location": "San Francisco, CA",
    "maxResults": 50
  }'
```

**You should get:**
- Real job postings from companies
- Actual skill requirements from job descriptions
- Up-to-date market demand data

---

## 💰 Cost Estimation

### Development/Testing Phase
- **OpenAI:** ~$1-5/month (testing only)
- **RapidAPI:** FREE (250 requests/month)
- **Total:** ~$1-5/month

### Production (1000 users)
- **OpenAI:** ~$20-50/month
  - 1000 users × 5 requests/month = 5000 requests
  - 5000 × $0.002 = $10 (+ usage variance)
- **RapidAPI:** $10/month (Pro plan - 10K requests)
- **Total:** ~$30-60/month

---

## 🔒 Security Best Practices

### ❌ DON'T Do This:
```yaml
# NEVER commit API keys to GitHub!
external:
  openai:
    api-key: sk-proj-1234567890...  # ❌ EXPOSED!
```

### ✅ DO This Instead:

**Option 1: Environment Variables**
```yaml
external:
  openai:
    api-key: ${OPENAI_API_KEY}  # Read from environment
```

Then set in terminal:
```bash
export OPENAI_API_KEY="sk-proj-..."
mvn spring-boot:run
```

**Option 2: application-local.yml (gitignored)**
1. Create `application-local.yml`
2. Add to `.gitignore`
3. Put secrets there
4. Use: `spring.profiles.active=local`

---

## 📊 Monitoring API Usage

### OpenAI Dashboard
- URL: https://platform.openai.com/usage
- Shows: Requests, tokens used, cost
- Set: Usage limits to prevent overspending

### RapidAPI Dashboard
- URL: https://rapidapi.com/developer/billing
- Shows: Request count, quota remaining
- Upgrade: When you hit limits

---

## 🐛 Troubleshooting

### Issue: "openai_configured": false

**Problem:** AI service not detecting API key

**Solutions:**
1. Check `.env` file exists in `backend/ai-service/`
2. Verify no extra spaces: `OPENAI_API_KEY=sk-...` (no spaces around `=`)
3. Restart Python service
4. Check logs for error messages

---

### Issue: "Invalid API key"

**Problem:** Key is wrong or expired

**Solutions:**
1. Regenerate key on OpenAI platform
2. Copy entire key (starts with `sk-proj-...`)
3. Check for hidden characters
4. Ensure no quotes around key in `.env`

---

### Issue: RapidAPI "Rate limit exceeded"

**Problem:** Used all 250 free requests

**Solutions:**
1. Wait for monthly reset
2. Upgrade to Pro plan ($10/month)
3. Temporarily use mock data: `spring.profiles.active=dev`

---

### Issue: "CORS error" from frontend

**Problem:** Frontend can't call backend APIs

**Solution:** Already configured! Check `SecurityConfig.java`:
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",  // React
    "http://localhost:5173"   // Vite
));
```

---

## 🎯 What Changes When Using Real APIs?

### Mock Data (Current)
- ✅ Instant responses
- ✅ No cost
- ✅ Perfect for development
- ❌ Fake job data
- ❌ Generic AI responses

### Real APIs (Production)
- ✅ Real job market data
- ✅ Personalized AI advice from GPT-4
- ✅ Up-to-date skill demands
- ❌ Costs money (but cheap!)
- ❌ Slightly slower (network calls)

---

## 🚀 When to Switch?

**Use Mock Data When:**
- Developing features
- Writing tests
- Demo/presentation
- No internet connection

**Use Real APIs When:**
- Production deployment
- Real users
- Need accurate data
- Want best AI quality

---

## ✅ Checklist: Ready for Real APIs

- [ ] Signed up for OpenAI
- [ ] Got OpenAI API key (starts with `sk-proj-...`)
- [ ] Added key to `backend/ai-service/.env`
- [ ] Tested: `curl http://localhost:8001/health` shows `openai_configured: true`
- [ ] Signed up for RapidAPI
- [ ] Subscribed to JSearch API (free tier)
- [ ] Got RapidAPI key
- [ ] Added key to `application.yml`
- [ ] Changed profile to `prod`
- [ ] Restarted both services
- [ ] Tested real job data sync
- [ ] Tested real AI advice

---

## 🎉 You're All Set!

Your SkillSync platform is now using real, production-grade APIs!

**Next Steps:**
1. Build the React frontend
2. Deploy to AWS
3. Add monitoring/analytics
4. Share with real users!

---

**Questions?** Check the main README or knowledge transfer document.
