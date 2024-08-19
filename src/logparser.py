import re
import matplotlib.pyplot as plt
from datetime import datetime

def parse_log_file(file_path):
    photo_upload_times = []
    metadata_upload_times = []
    total_times = []
    current_total = 0
    start_time = None
    
    with open(file_path, 'r') as file:
        for line in file:
            timestamp_match = re.match(r'(\w{3} \d{2}, \d{4} \d{1,2}:\d{2}:\d{2} [APM]{2})', line)
            if timestamp_match:
                current_time = datetime.strptime(timestamp_match.group(1), '%b %d, %Y %I:%M:%S %p')
                if start_time is None:
                    start_time = current_time
            
            if "Time taken to upload photo:" in line:
                time = int(re.search(r'(\d+) ms', line).group(1))
                photo_upload_times.append(time)
                current_total += time
            elif "Time taken to upload metadata:" in line:
                time = int(re.search(r'(\d+) ms', line).group(1))
                metadata_upload_times.append(time)
                current_total += time
                total_times.append(current_total)
                current_total = 0
    
    end_time = current_time
    total_runtime = (end_time - start_time).total_seconds()
    
    return photo_upload_times, metadata_upload_times, total_times, total_runtime

def calculate_average(times):
    return sum(times) / len(times) if times else 0

def create_line_chart(photo_times, metadata_times, total_times):
    plt.figure(figsize=(12, 6))
    
    plt.plot(range(1, len(photo_times) + 1), photo_times, label='Photo Upload')
    plt.plot(range(1, len(metadata_times) + 1), metadata_times, label='Metadata Upload')
    plt.plot(range(1, len(total_times) + 1), total_times, label='Total Upload')
    
    plt.title('Upload Times')
    plt.xlabel('Upload Number')
    plt.ylabel('Time (ms)')
    plt.legend()
    plt.grid(True)
    
    plt.savefig('upload_times_5_threads.png')
    plt.close()

def main():
    log_file_path = '/Users/nisssaarg/Desktop/marketLocation/uploader.log'  # Update this path if your log file is located elsewhere
    
    photo_times, metadata_times, total_times, total_runtime = parse_log_file(log_file_path)
    
    print(f"Number of photo uploads: {len(photo_times)}")
    print(f"Number of metadata uploads: {len(metadata_times)}")
    
    avg_photo_time = calculate_average(photo_times)
    avg_metadata_time = calculate_average(metadata_times)
    avg_total_time = calculate_average(total_times)
    
    print(f"Average photo upload time: {avg_photo_time:.2f} ms")
    print(f"Average metadata upload time: {avg_metadata_time:.2f} ms")
    print(f"Average total upload time: {avg_total_time:.2f} ms")
    print(f"Total runtime: {total_runtime:.2f} seconds")
    
    create_line_chart(photo_times, metadata_times, total_times)
    # print("Line chart saved as 'upload_times_line_chart.png'")

if __name__ == "__main__":
    main()