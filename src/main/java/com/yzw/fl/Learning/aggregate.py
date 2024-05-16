import os
import torch


def load_models(directory, device):
    models = []
    for filename in os.listdir(directory):
        if filename.endswith('.pth') and filename != 'model_state_dict.pth':
            model_path = os.path.join(directory, filename)
            model = torch.load(model_path, map_location=device)
            models.append(model)
    return models

def aggregate_models(models):
    # Assume all models have the same structure
    aggregated_model = {}
    num_models = len(models)

    for key in models[0].keys():
        # Initialize the aggregated_model[key] with zeros of the same shape as models[0][key]
        aggregated_model[key] = torch.zeros_like(models[0][key])

        # Sum up all model parameters
        for model in models:
            aggregated_model[key] += model[key]

        # Average the parameters
        aggregated_model[key] /= num_models

    return aggregated_model

def save_model(model, path):
    torch.save(model, path)


current_file_path = os.path.abspath(__file__)
current_working_directory = os.path.dirname(current_file_path)
directory = os.path.join(current_working_directory, ".")
if torch.cuda.is_available():
    device = torch.device("cuda")
else:
    device = torch.device("cpu")
models = load_models(directory, device)
if models:
    aggregated_model = aggregate_models(models)
    save_model(aggregated_model, os.path.join(current_working_directory, "model_state_dict.pth"))
    print("Models aggregated and saved to " + os.path.join(current_working_directory, "model_state_dict.pth"))
else:
    print("No models found for aggregation.")
