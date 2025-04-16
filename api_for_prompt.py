from groq import Groq
import yaml

api_key = None
CONFIG_PATH = r"config.yaml"

with open(CONFIG_PATH) as file:
    data = yaml.load(file, Loader=yaml.FullLoader)
    api_key = data['GROQ_API_KEY']

def prompt_result(sensor_data):

    prompt = '''
    You are an AI bot designed to provide a response about whether the user is available or not. You are given the user's mobile sensor data and your job is to provide a response using the data and providing accurate reasoning for the same. The sensor data contains the following information:
    1. Time: ts, DayOfWeek (2–4), HourOfDay (7–23).
    2. Device: Battery (45–100%), Screen_Value (Locked/Unlocked), Ringer_Mode (VIBRATE/SILENT).
    3. Activity: Mostly still, some on_foot, in_vehicle.
    4. Environment: Loc_Type (mostly Home), Noise_Value (36–96), Light_Value (0–955), Temperature (15–32°C).
    5. Apps: WhatsApp, launcher, occasional Gmail, Audible.
    6. Interaction: Timestamps like timeSinceLastScreenUnlocked (e.g., 1.8–72 minutes).
    7. Events: Few meetings (e.g., CSCW).
    Give the automated response in json format only
    '''

    user_content = sensor_data

    client = Groq(
        api_key = api_key
    )

    # messages = [
    #     {
    #         "role": "system",
    #         "content": prompt
    #     },
    #     {
    #         "role": "user",
    #         "content": user_content
    #     }
    # ]

    response = client.chat.completions.create(
        messages = [
        {
            "role": "system",
            "content": prompt
        },
        {
            "role": "user",
            "content": user_content
        }
    ],
        model = "llama-3.3-70b-versatile",
        temperature=0.5,
        max_completion_tokens=1024,
    )

    data = response.choices[0].message.content

    return data
