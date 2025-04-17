from groq import Groq
import yaml
import json
import sys
from typing import Optional, Dict, Any

# Constants
CONFIG_PATH = "config.yaml"
DEFAULT_MODEL = "llama3-70b-8192"
MAX_RETRIES = 3

def load_config() -> Dict[str, Any]:
    """Load configuration from YAML file with error handling."""
    try:
        with open(CONFIG_PATH) as file:
            return yaml.safe_load(file)  # Using safe_load for security
    except FileNotFoundError:
        print(f"Error: Config file not found at {CONFIG_PATH}")
        sys.exit(1)
    except yaml.YAMLError as e:
        print(f"Error parsing YAML config: {str(e)}")
        sys.exit(1)
    except Exception as e:
        print(f"Unexpected error loading config: {str(e)}")
        sys.exit(1)

def validate_api_key(api_key: Optional[str]) -> str:
    """Validate the API key is present and properly formatted."""
    if not api_key:
        print("Error: GROQ_API_KEY not found in config.yaml")
        sys.exit(1)
    if not isinstance(api_key, str) or len(api_key) < 20:  # Basic validation
        print("Error: Invalid GROQ_API_KEY format")
        sys.exit(1)
    return api_key

def create_groq_client(api_key: str) -> Groq:
    """Create and return Groq client with error handling."""
    try:
        return Groq(api_key=api_key)
    except Exception as e:
        print(f"Error initializing Groq client: {str(e)}")
        sys.exit(1)

def generate_prompt(sensor_data: str) -> str:
    """Generate the system prompt for the LLM."""
    return '''
    You are an AI bot designed to provide a response about whether the user is available or not. 
    You are given the user's mobile sensor data and your job is to provide a response using the data 
    and providing accurate reasoning for the same. The sensor data contains the following information:
    1. Time: ts, DayOfWeek (2-4), HourOfDay (7-23).
    2. Device: Battery (45-100%), Screen_Value (Locked/Unlocked), Ringer_Mode (VIBRATE/SILENT).
    3. Activity: Mostly still, some on_foot, in_vehicle.
    4. Environment: Loc_Type (mostly Home), Noise_Value (36-96), Light_Value (0-955), Temperature (15-32°C).
    5. Apps: WhatsApp, launcher, occasional Gmail, Audible.
    6. Interaction: Timestamps like timeSinceLastScreenUnlocked (e.g., 1.8-72 minutes).
    7. Events: Few meetings (e.g., CSCW).
    Give the automated response in valid JSON format only with the following structure:
    {
        "availability_status": "available/unavailable",
        "confidence_score": 0-1,
        "reasoning": "detailed explanation",
        "suggested_response_time": "HH:MM format if available"
    }
    '''

def prompt_result(sensor_data: str) -> Dict[str, Any]:
    """Main function to get response from LLM with comprehensive error handling."""
    # Load and validate configuration
    config = load_config()
    api_key = validate_api_key(config.get('GROQ_API_KEY'))
    
    # Initialize Groq client
    client = create_groq_client(api_key)
    
    # Prepare prompt and messages
    prompt = generate_prompt(sensor_data)
    messages = [
        {"role": "system", "content": prompt},
        {"role": "user", "content": sensor_data}
    ]
    
    # Attempt API call with retries
    for attempt in range(MAX_RETRIES):
        try:
            response = client.chat.completions.create(
                messages=messages,
                model=DEFAULT_MODEL,
                temperature=0.5,
                max_tokens=1024,
                response_format={"type": "json_object"}  # Ensure JSON output
            )
            
            # Parse and validate response
            response_content = response.choices[0].message.content
            try:
                return json.loads(response_content)
            except json.JSONDecodeError:
                print(f"Error: LLM response is not valid JSON. Attempt {attempt + 1}/{MAX_RETRIES}")
                if attempt == MAX_RETRIES - 1:
                    print(f"Raw response: {response_content}")
                    return {
                        "error": "Invalid JSON response from LLM",
                        "raw_response": response_content
                    }
                continue
                
        except Exception as e:
            print(f"API call failed (attempt {attempt + 1}/{MAX_RETRIES}): {str(e)}")
            if attempt == MAX_RETRIES - 1:
                return {
                    "error": "Failed to get response from LLM after retries",
                    "details": str(e)
                }
    
    return {"error": "Unexpected error in prompt_result"}

# Example usage (for testing)
if __name__ == "__main__":
    test_data = """
    Current sensor data:
    - Time: Thursday 14:30
    - Battery: 78%
    - Screen: Unlocked
    - Activity: still
    - Location: Home
    """
    
    try:
        result = prompt_result(test_data)
        print("LLM Response:")
        print(json.dumps(result, indent=2))
    except Exception as e:
        print(f"Fatal error in main execution: {str(e)}")
        sys.exit(1)
